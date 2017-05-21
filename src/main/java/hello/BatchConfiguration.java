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
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

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
    public FlatFileItemReader<PollutionData> reader() {
        FlatFileItemReader<PollutionData> reader = new FlatFileItemReader<PollutionData>();
        reader.setResource(new ClassPathResource("data.csv"));
        reader.setLinesToSkip(2);
        DefaultLineMapper<PollutionData> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(new DelimitedLineTokenizer(";"));
        lineMapper.setFieldSetMapper(new PlayerFieldSetMapper());
        reader.setLineMapper(lineMapper);


//        reader.setLineMapper(new DefaultLineMapper<PollutionData>() {{
//            setLineTokenizer(new DelimitedLineTokenizer(";") {{
//                setNames(new String[] { "stationCode", "stationName", "dailyMean", "measuringMethod", "firstMeasuringDate",
//                "recentMeasuringDate", "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"});
//            }});
//            setFieldSetMapper(new BeanWrapperFieldSetMapper<PollutionData>() {{
//                setTargetType(PollutionData.class);
//            }});
//        }});
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
    public Job importUserJob(JobCompletionNotificationListener listener) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1())
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<PollutionData, PollutionData> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }
    // end::jobstep[]
}
