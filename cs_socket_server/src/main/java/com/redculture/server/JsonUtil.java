package com.redculture.server;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * 极简 JSON 工具（零第三方依赖，仅供 HTTP 接口适配器使用）
 *
 * 序列化：任意 Java 对象 -> JSON 字符串（POJO 通过 getter 反射，password 字段不输出）
 * 反解析：JSON 字符串 -> Map / List / String / Long / Double / Boolean / null
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    // ==================== 序列化：Java 对象 -> JSON ====================

    public static String toJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        writeValue(sb, obj);
        return sb.toString();
    }

    private static void writeValue(StringBuilder sb, Object obj) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String || obj instanceof Character) {
            writeString(sb, obj.toString());
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj);
        } else if (obj instanceof java.util.Date) {
            // 日期时间统一按字符串输出，如 2026-09-10 08:30:00.0
            writeString(sb, obj.toString());
        } else if (obj instanceof Map) {
            writeMap(sb, (Map<?, ?>) obj);
        } else if (obj instanceof Collection) {
            writeCollection(sb, (Collection<?>) obj);
        } else if (obj instanceof Object[]) {
            writeCollection(sb, java.util.Arrays.asList((Object[]) obj));
        } else {
            writeBean(sb, obj);
        }
    }

    private static void writeMap(StringBuilder sb, Map<?, ?> map) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            writeString(sb, String.valueOf(e.getKey()));
            sb.append(':');
            writeValue(sb, e.getValue());
            first = false;
        }
        sb.append('}');
    }

    private static void writeCollection(StringBuilder sb, Collection<?> list) {
        sb.append('[');
        boolean first = true;
        for (Object item : list) {
            if (!first) {
                sb.append(',');
            }
            writeValue(sb, item);
            first = false;
        }
        sb.append(']');
    }

    /** 通过 getter 反射序列化 POJO；password 属性不输出，避免口令泄露 */
    private static void writeBean(StringBuilder sb, Object bean) {
        sb.append('{');
        boolean first = true;
        for (Method m : bean.getClass().getMethods()) {
            String prop = propertyOf(m);
            if (prop == null || "class".equals(prop) || "password".equals(prop)) {
                continue;
            }
            try {
                if (!first) {
                    sb.append(',');
                }
                writeString(sb, prop);
                sb.append(':');
                writeValue(sb, m.invoke(bean));
                first = false;
            } catch (Exception ignored) {
                // 单个属性读取失败不影响整体输出
            }
        }
        sb.append('}');
    }

    /** 把 getXxx / isXxx 转成属性名 xxx；不符合 getter 规范时返回 null */
    private static String propertyOf(Method m) {
        if (m.getParameterCount() != 0 || m.getReturnType() == void.class) {
            return null;
        }
        String n = m.getName();
        String raw = null;
        if (n.startsWith("get") && n.length() > 3) {
            raw = n.substring(3);
        } else if (n.startsWith("is") && n.length() > 2
                && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
            raw = n.substring(2);
        }
        return raw == null ? null : Character.toLowerCase(raw.charAt(0)) + raw.substring(1);
    }

    private static void writeString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    sb.append(c < 0x20 ? String.format("\\u%04x", (int) c) : c);
            }
        }
        sb.append('"');
    }

    // ==================== 解析：JSON -> Map / List / 基本类型 ====================

    public static Object parse(String json) {
        return new Parser(json).parseValue();
    }

    /** 递归下降解析器（支持对象、数组、字符串、数字、布尔、null） */
    private static final class Parser {
        private final String s;
        private int i;

        Parser(String s) {
            this.s = s == null ? "" : s;
        }

        Object parseValue() {
            skipWs();
            if (i >= s.length()) {
                return null;
            }
            char c = s.charAt(i);
            switch (c) {
                case '{': return parseObject();
                case '[': return parseArray();
                case '"': return parseString();
                case 't': expect("true");  return Boolean.TRUE;
                case 'f': expect("false"); return Boolean.FALSE;
                case 'n': expect("null");  return null;
                default:  return parseNumber();
            }
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            i++; // 跳过 {
            skipWs();
            if (peek('}')) {
                i++;
                return map;
            }
            while (true) {
                skipWs();
                String key = parseString();
                skipWs();
                expect(':');
                map.put(key, parseValue());
                skipWs();
                if (peek(',')) {
                    i++;
                } else {
                    expect('}');
                    return map;
                }
            }
        }

        private java.util.List<Object> parseArray() {
            java.util.List<Object> list = new java.util.ArrayList<>();
            i++; // 跳过 [
            skipWs();
            if (peek(']')) {
                i++;
                return list;
            }
            while (true) {
                list.add(parseValue());
                skipWs();
                if (peek(',')) {
                    i++;
                } else {
                    expect(']');
                    return list;
                }
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\' && i < s.length()) {
                    char e = s.charAt(i++);
                    switch (e) {
                        case '"':  sb.append('"');  break;
                        case '\\': sb.append('\\'); break;
                        case '/':  sb.append('/');  break;
                        case 'b':  sb.append('\b'); break;
                        case 'f':  sb.append('\f'); break;
                        case 'n':  sb.append('\n'); break;
                        case 'r':  sb.append('\r'); break;
                        case 't':  sb.append('\t'); break;
                        case 'u':
                            sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                            i += 4;
                            break;
                        default: sb.append(e);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalArgumentException("JSON 字符串未闭合");
        }

        private Object parseNumber() {
            int start = i;
            while (i < s.length() && "-+0123456789.eE".indexOf(s.charAt(i)) >= 0) {
                i++;
            }
            String num = s.substring(start, i);
            if (num.isEmpty()) {
                throw new IllegalArgumentException("JSON 格式错误，位置 " + i);
            }
            return num.contains(".") || num.contains("e") || num.contains("E")
                    ? Double.parseDouble(num) : Long.parseLong(num);
        }

        private void skipWs() {
            while (i < s.length() && " \t\r\n".indexOf(s.charAt(i)) >= 0) {
                i++;
            }
        }

        private boolean peek(char c) {
            return i < s.length() && s.charAt(i) == c;
        }

        private void expect(char c) {
            if (!peek(c)) {
                throw new IllegalArgumentException("JSON 期望字符 '" + c + "'，位置 " + i);
            }
            i++;
        }

        private void expect(String word) {
            if (!s.startsWith(word, i)) {
                throw new IllegalArgumentException("JSON 期望 " + word + "，位置 " + i);
            }
            i += word.length();
        }
    }
}
