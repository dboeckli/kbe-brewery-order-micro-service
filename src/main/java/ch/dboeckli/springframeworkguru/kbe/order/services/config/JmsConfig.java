package ch.dboeckli.springframeworkguru.kbe.order.services.config;

import org.springframework.context.annotation.Bean;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Created by jt on 2019-09-07.
 */
@Component
public class JmsConfig {
    @Bean // Serialize message content to json using TextMessage
    public MessageConverter jacksonJmsMessageConverter(JsonMapper objectMapper) {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(objectMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
