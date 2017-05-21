package hello;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {


    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    // tag::readerwriterprocessor[]
    @Bean
    public FlatFileItemReader reader() throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        FlatFileItemReader reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("data.csv"));

        //TODO the following line functionality should be moved to the user side
        reader.setLinesToSkip(2);
       // DefaultLineMapper lineMapper = new DefaultLineMapper<>();
       // lineMapper.setLineTokenizer(new DelimitedLineTokenizer(";"));


        ClassLoader loader = URLClassLoader.newInstance(
                new URL[]{new URL("file://program.jar")},
                getClass().getClassLoader()
        );
//            Class<?> clazz = Class.forName("mypackage.MyClass", true, loader);

        //  URLClassLoader child = new URLClassLoader (new URL[] {new URL("file://./first-1.0-SNAPSHOT.jar")}, Deet.class.getClassLoader());

       // Class classToLoad = Class.forName("hello.PlayerFieldSetMapper", true, loader);
        // Method method = classToLoad.getDeclaredMethod ("hello");
       // Object instance = classToLoad.newInstance();
        //  Object result = method.invoke (instance);

        //  System.out.println(result);

       PollutionData pollutionData = (PollutionData) Class.forName("hello.PollutionData", true, loader).newInstance();


      //  lineMapper.setFieldSetMapper((FieldSetMapper<PollutionData>) instance);
       // reader.setLineMapper(lineMapper);


        reader.setLineMapper(new DefaultLineMapper<Object>() {{
            setLineTokenizer(new DelimitedLineTokenizer(";") {{
                setNames(pollutionData.getClassVariables());
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Object>() {{
                setTargetType(pollutionData.getClass());
            }});
        }});
        return reader;
    }

    @Bean
    public PollutionDataItemProcessor processor() {
        return new PollutionDataItemProcessor();
    }


    @Bean
    public JdbcBatchItemWriter<PollutionData> writer() {

        JdbcBatchItemWriter<PollutionData> writer = new JdbcBatchItemWriter<PollutionData>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<PollutionData>());
        writer.setSql("INSERT INTO pollution_data (station_code, station_name, daily_mean, measuring_method, first_measuring_date, " +
                "recent_measuring_date, jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec) VALUES (:stationCode, " +
                ":stationName, :dailyMean, :measuringMethod, :firstMeasuringDate, " +
                ":recentMeasuringDate, :jan, :feb, :mar, :apr, :may, :jun, :jul, :aug, :sep, :oct, :nov, :dec)");
        writer.setDataSource(dataSource);
        return writer;
    }
    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener) throws NoSuchMethodException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, MalformedURLException {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1())
                .end()
                .build();
    }

    @Bean
    public Step step1() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, MalformedURLException, ClassNotFoundException {
        return stepBuilderFactory.get("step1")
                .<PollutionData, PollutionData>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }
    // end::jobstep[]
}
