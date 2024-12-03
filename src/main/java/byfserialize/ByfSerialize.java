package byfserialize;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;

public class B {


  @SneakyThrows
  public String serialize(Object object) {
    StringBuilder stringBuilder = new StringBuilder();
    Class<?> clazz = object.getClass();
    for (Field field : clazz.getDeclaredFields()) {
      field.setAccessible(true);
      String name = field.getName();
      Object value = field.get(object);
      int typeKey = getTypeKey(field.getType());
      stringBuilder.append("%s:<%s:%s>".formatted(name, value, typeKey)).append(",");
    }
    return stringBuilder.substring(0, stringBuilder.length() - 1);
  }


  @SneakyThrows
  public <T> T deserialize(String des, Class<T> clazz) {
    T object = createInstanceWithDefaults(clazz);
    Map<String, Pair<Object, Class<?>>> values = parseValues(des);
    for (Field field : clazz.getDeclaredFields()) {
      field.setAccessible(true);
      Pair<Object, Class<?>> pair = values.get(field.getName());
      if (pair != null) {
        Object castedValue = castToType(pair.getKey(), pair.getValue());
        field.set(object, castedValue);
      }
    }
    return object;
  }

  @SuppressWarnings("unchecked")
  @SneakyThrows
  private <T> T createInstanceWithDefaults(Class<T> clazz) {
    for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
      if (constructor.getParameterCount() == 0) {
        constructor.setAccessible(true);
        return (T) constructor.newInstance();
      }
    }
    Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
    constructor.setAccessible(true);
    Class<?>[] parameterTypes = constructor.getParameterTypes();
    Object[] parameters = new Object[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      parameters[i] = getDefaultValue(parameterTypes[i]);
    }
    return (T) constructor.newInstance(parameters);
  }

  private Object getDefaultValue(Class<?> type) {
    if (type == int.class || type == Integer.class) {
      return 0;
    } else if (type == double.class || type == Double.class) {
      return 0.0;
    } else if (type == float.class || type == Float.class) {
      return 0.0f;
    } else if (type == boolean.class || type == Boolean.class) {
      return false;
    } else if (type == char.class || type == Character.class) {
      return '\0';
    } else if (type == byte.class || type == Byte.class) {
      return (byte) 0;
    } else if (type == short.class || type == Short.class) {
      return (short) 0;
    } else if (type == long.class || type == Long.class) {
      return 0L;
    } else if (type == String.class) {
      return "";
    }
    return null;
  }

  private Map<String, Pair<Object, Class<?>>> parseValues(String des) {
    Map<String, Pair<Object, Class<?>>> values = new HashMap<>();
    String regex = "(\\w+):<([^:]+):(\\d+)>";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(des);
    while (matcher.find()) {
      String field = matcher.group(1);
      Object value = matcher.group(2);
      int typeKey = Integer.parseInt(matcher.group(3));
      values.put(field, Pair.of(value, getTypeClass(typeKey)));
    }
    return values;
  }

  private Object castToType(Object value, Class<?> type) {
    if (type == int.class || type == Integer.class) {
      return Integer.parseInt((String) value);
    } else if (type == double.class || type == Double.class) {
      return Double.parseDouble((String) value);
    } else if (type == float.class || type == Float.class) {
      return Float.parseFloat((String) value);
    } else if (type == String.class) {
      return value;
    }
    throw new IllegalArgumentException("Unsupported type: " + type);
  }

  private int getTypeKey(Class<?> type) {
    if (type == int.class || type == Integer.class) {
      return 0;
    } else if (type == String.class) {
      return 1;
    } else if (type == double.class || type == Double.class) {
      return 2;
    } else if (type == float.class || type == Float.class) {
      return 3;
    }
    throw new IllegalArgumentException("Unsupported type: " + type);
  }

  private Class<?> getTypeClass(int key) {
    return switch (key) {
      case 0 -> Integer.class;
      case 1 -> String.class;
      case 2 -> Double.class;
      case 3 -> Float.class;
      default -> throw new IllegalArgumentException("Invalid key: " + key);
    };
  }
}