package com.aryak.learn.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizer;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizingMessageSource;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.messaging.MessageChannel;

import java.io.File;

import static org.apache.commons.net.ftp.FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE;

@Slf4j
@Configuration
public class FtpConfig {

    @Bean
    public SessionFactory<FTPFile> ftpSessionFactory() {
        DefaultFtpSessionFactory factory = new DefaultFtpSessionFactory();
        factory.setHost("localhost");
        factory.setPort(21);
        factory.setUsername("demo");
        factory.setPassword("demo123");
        factory.setClientMode(PASSIVE_LOCAL_DATA_CONNECTION_MODE);
        // Add connection resilience
//        factory.setConnectTimeout(10000); // 10 seconds
//        factory.setDefaultTimeout(30000);  // 30 seconds
//        factory.setDataTimeout(30000);     // 30 seconds
        return new CachingSessionFactory<>(factory);
    }

    @Bean
    public RemoteFileTemplate<FTPFile> ftpRemoteFileTemplate(SessionFactory<FTPFile> factory) {
        RemoteFileTemplate<FTPFile> template = new RemoteFileTemplate<>(factory);
        template.setAutoCreateDirectory(true);
        return template;
    }

    @Bean
    public FtpInboundFileSynchronizer ftpInboundFileSynchronizer(SessionFactory<FTPFile> sessionFactory) {
        FtpInboundFileSynchronizer synchronizer = new FtpInboundFileSynchronizer(sessionFactory);
        synchronizer.setDeleteRemoteFiles(false);
        synchronizer.setRemoteDirectory("/"); // /home/demo
        synchronizer.setPreserveTimestamp(false);
        return synchronizer;
    }

    @Bean
    @InboundChannelAdapter(channel = "ftpChannel", poller = @Poller(fixedDelay = "5000"))
    public MessageSource<File> ftpMessageSource(FtpInboundFileSynchronizer synchronizer) {
        FtpInboundFileSynchronizingMessageSource source =
                new FtpInboundFileSynchronizingMessageSource(synchronizer);
        source.setLocalDirectory(new File("ftp-downloaded"));
        source.setAutoCreateLocalDirectory(true);
        return source;
    }

    @Bean
    public MessageChannel ftpChannel() {
        return new DirectChannel();
    }


    @ServiceActivator(inputChannel = "ftpChannel")
    public void triggerBatchJob(File file) throws Exception {

        log.info("=== FTP File Downloaded ===");
        log.info("File name: '{}', path: '{}', size: {} bytes", file.getName(), file.getAbsolutePath(), file.length());

    }

}
