package xyz.yellowstrawberry.commander.utils;

public interface ParameterConverter<T> {
    T convert(String param);
}
