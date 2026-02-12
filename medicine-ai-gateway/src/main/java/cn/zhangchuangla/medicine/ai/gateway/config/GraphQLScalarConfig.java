package cn.zhangchuangla.medicine.ai.gateway.config;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.*;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

/**
 * GraphQL 标量配置
 *
 * @author Chuang
 * <p>
 * created on 2026/2/12
 */
@Configuration
public class GraphQLScalarConfig {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 注册 DateTime 与 BigDecimal 标量。
     */
    @Bean
    public RuntimeWiringConfigurer scalarConfigurer() {
        GraphQLScalarType dateTimeScalar = GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("DateTime scalar, format: yyyy-MM-dd HH:mm:ss")
                .coercing(new Coercing<Date, String>() {
                    @Override
                    public String serialize(@NonNull Object dataFetcherResult,
                                            @NonNull GraphQLContext graphQLContext,
                                            @NonNull Locale locale) throws CoercingSerializeException {
                        Instant instant = toInstant(dataFetcherResult);
                        if (instant == null) {
                            throw new CoercingSerializeException("DateTime serialization value is invalid: " + dataFetcherResult);
                        }
                        return DATE_TIME_FORMATTER.format(instant.atZone(ZONE_ID));
                    }

                    @Override
                    public Date parseValue(@NonNull Object input,
                                           @NonNull GraphQLContext graphQLContext,
                                           @NonNull Locale locale) throws CoercingParseValueException {
                        if (!(input instanceof String value)) {
                            throw new CoercingParseValueException("DateTime parse value must be String");
                        }
                        return parseDate(value, CoercingParseValueException::new);
                    }

                    @Override
                    public Date parseLiteral(@NonNull Value<?> input,
                                             @NonNull CoercedVariables variables,
                                             @NonNull GraphQLContext graphQLContext,
                                             @NonNull Locale locale) throws CoercingParseLiteralException {
                        if (!(input instanceof StringValue stringValue)) {
                            throw new CoercingParseLiteralException("DateTime literal must be String");
                        }
                        return parseDate(stringValue.getValue(), CoercingParseLiteralException::new);
                    }

                    private Instant toInstant(Object value) {
                        return switch (value) {
                            case Date date -> date.toInstant();
                            case Instant instant -> instant;
                            case LocalDateTime localDateTime -> localDateTime.atZone(ZONE_ID).toInstant();
                            case LocalDate localDate -> localDate.atStartOfDay(ZONE_ID).toInstant();
                            case OffsetDateTime offsetDateTime -> offsetDateTime.toInstant();
                            case ZonedDateTime zonedDateTime -> zonedDateTime.toInstant();
                            case null, default -> null;
                        };
                    }

                    private <E extends RuntimeException> Date parseDate(String value,
                                                                        DateParseExceptionFactory<E> exceptionFactory) {
                        try {
                            LocalDateTime localDateTime = LocalDateTime.parse(value, DATE_TIME_FORMATTER);
                            return Date.from(localDateTime.atZone(ZONE_ID).toInstant());
                        } catch (DateTimeParseException ex) {
                            throw exceptionFactory.create("DateTime format must be yyyy-MM-dd HH:mm:ss", ex);
                        }
                    }
                })
                .build();

        GraphQLScalarType bigDecimalScalar = GraphQLScalarType.newScalar()
                .name("BigDecimal")
                .description("BigDecimal scalar, serialized as String to preserve precision")
                .coercing(new Coercing<BigDecimal, String>() {
                    @Override
                    public String serialize(@NonNull Object dataFetcherResult,
                                            @NonNull GraphQLContext graphQLContext,
                                            @NonNull Locale locale) throws CoercingSerializeException {
                        BigDecimal decimal = toBigDecimal(dataFetcherResult);
                        if (decimal == null) {
                            throw new CoercingSerializeException("BigDecimal serialization value is invalid: " + dataFetcherResult);
                        }
                        return decimal.toPlainString();
                    }

                    @Override
                    public BigDecimal parseValue(@NonNull Object input,
                                                 @NonNull GraphQLContext graphQLContext,
                                                 @NonNull Locale locale) throws CoercingParseValueException {
                        BigDecimal decimal = toBigDecimal(input);
                        if (decimal == null) {
                            throw new CoercingParseValueException("BigDecimal parse value is invalid: " + input);
                        }
                        return decimal;
                    }

                    @Override
                    public BigDecimal parseLiteral(@NonNull Value<?> input,
                                                   @NonNull CoercedVariables variables,
                                                   @NonNull GraphQLContext graphQLContext,
                                                   @NonNull Locale locale) throws CoercingParseLiteralException {
                        return switch (input) {
                            case StringValue stringValue ->
                                    parseBigDecimal(stringValue.getValue(), CoercingParseLiteralException::new);
                            case IntValue intValue -> new BigDecimal(intValue.getValue());
                            case FloatValue floatValue -> floatValue.getValue();
                            default ->
                                    throw new CoercingParseLiteralException("BigDecimal literal must be String, Int or Float");
                        };
                    }

                    private BigDecimal toBigDecimal(Object value) {
                        return switch (value) {
                            case BigDecimal decimal -> decimal;
                            case Number number -> new BigDecimal(number.toString());
                            case String text -> {
                                try {
                                    yield new BigDecimal(text);
                                } catch (NumberFormatException ex) {
                                    yield null;
                                }
                            }
                            case null, default -> null;
                        };
                    }

                    private <E extends RuntimeException> BigDecimal parseBigDecimal(String value,
                                                                                    DecimalParseExceptionFactory<E> exceptionFactory) {
                        try {
                            return new BigDecimal(value);
                        } catch (NumberFormatException ex) {
                            throw exceptionFactory.create("BigDecimal format is invalid", ex);
                        }
                    }
                })
                .build();

        return wiringBuilder -> wiringBuilder.scalar(dateTimeScalar).scalar(bigDecimalScalar);
    }

    @FunctionalInterface
    private interface DateParseExceptionFactory<E extends RuntimeException> {
        E create(String message, Throwable cause);
    }

    @FunctionalInterface
    private interface DecimalParseExceptionFactory<E extends RuntimeException> {
        E create(String message, Throwable cause);
    }
}
