package com.wixpress.petri;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.petri.*;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 9/9/14
* Time: 5:17 PM
* To change this template use File | Settings | File Templates.
*/
class PetriServerFactory {
    private final DBConfig dbConfig;
    private final int port;
    private final static Long lookBackForReportsDelta = 30000l;
    private MetricsReportsDao metricsReportsDao;
    private ExperimentsDao experimentsDao;
    private PetriNotifier notifier;

    public PetriServerFactory(int port, DBConfig dbConfig) {
        this.dbConfig = dbConfig;
        this.port = port;
    }

    public PetriRpcServer makePetriServer(ObjectMapper objectMapper) {
        BasicDataSource ds = new BasicDataSource();
        ds.setUsername(dbConfig.username);
        ds.setPassword(dbConfig.password);
        ds.setUrl(dbConfig.url);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

        MappingErrorHandler mappingErrorHandler = new ConsoleMappingErrorHandler();
        PetriMapper experimentMapper = new ExperimentMapper(objectMapper,mappingErrorHandler);
        experimentsDao = new JdbcExperimentsDao(jdbcTemplate, experimentMapper);
        Clock clock = new JodaTimeClock();
        PetriMapper specMapper = new SpecMapper(objectMapper,mappingErrorHandler);
        SpecsDao specsDao = new JdbcSpecsDao(jdbcTemplate,specMapper);
        notifier = new NoopPetriNotifier();
        metricsReportsDao = new JdbcMetricsReportsDao(jdbcTemplate, lookBackForReportsDelta);
        UserStateDao userStateDao = new JdbcUserStateDao(jdbcTemplate);
        return new PetriRpcServer(experimentsDao,clock,specsDao, notifier, metricsReportsDao, userStateDao);
    }

    public ConductionKeeper makeConductionKeeper(int conductionLimitIntervalInMillis){
        return new ConductionKeeper(
                new JodaTimeClock(), metricsReportsDao, experimentsDao,
                new ScheduledThreadPoolExecutor(1), conductionLimitIntervalInMillis,
                notifier);
    }



    private static class NoopPetriNotifier implements PetriNotifier {
        @Override
        public void notify(String title, String message, String... users) {

        }

        @Override
        public void notify(String title, String message, Boolean notifyCaptain, List<String> recipients) {

        }
    }

    private static MailRecipients defaultMailRecipients(){
        return new MailRecipients(Collections.<InternetAddress>emptySet(), Collections.<InternetAddress>emptySet());

    }

    private static class ConsoleMappingErrorHandler implements MappingErrorHandler {
        @Override
        public void handleError(String string, String entityDescription, IOException e) {
            e.printStackTrace();      // TODO: Log these
        }
    }
}
