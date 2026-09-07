package com.eyecode.lessons.catalog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Json {
    private final String input;
    private int index;
    private Json(String input) { this.input = input == null ? "" : input; }
    static Object parse(String input) { return new Json(input).value(); }
    private Object value() { whitespace(); Object value = nested(); whitespace(); if (index != input.length()) throw error(); return value; }
    private Object nested() { whitespace(); if (index >= input.length()) throw error(); return switch (input.charAt(index)) {
        case '{' -> object(); case '[' -> array(); case '"' -> string(); case 't' -> literal("true", Boolean.TRUE);
        case 'f' -> literal("false", Boolean.FALSE); case 'n' -> literal("null", null); default -> number(); }; }
    private Map<String, Object> object() { Map<String, Object> result = new LinkedHashMap<>(); index++; whitespace(); if (consume('}')) return result;
        while (true) { String key = string(); whitespace(); require(':'); result.put(key, nested()); whitespace(); if (consume('}')) return result; require(','); whitespace(); } }
    private List<Object> array() { List<Object> result = new ArrayList<>(); index++; whitespace(); if (consume(']')) return result;
        while (true) { result.add(nested()); whitespace(); if (consume(']')) return result; require(','); } }
    private String string() { require('"'); StringBuilder result = new StringBuilder(); while (index < input.length()) { char c = input.charAt(index++); if (c == '"') return result.toString(); if (c != '\\') { result.append(c); continue; } if (index >= input.length()) throw error(); char escaped = input.charAt(index++); switch (escaped) { case '"', '\\', '/' -> result.append(escaped); case 'b' -> result.append('\b'); case 'f' -> result.append('\f'); case 'n' -> result.append('\n'); case 'r' -> result.append('\r'); case 't' -> result.append('\t'); case 'u' -> { if (index + 4 > input.length()) throw error(); result.append((char) Integer.parseInt(input.substring(index, index + 4), 16)); index += 4; } default -> throw error(); } } throw error(); }
    private Object literal(String expected, Object value) { if (!input.startsWith(expected, index)) throw error(); index += expected.length(); return value; }
    private Number number() { int start = index; while (index < input.length() && "-+0123456789.eE".indexOf(input.charAt(index)) >= 0) index++; try { String value = input.substring(start, index); return value.contains(".") || value.contains("e") || value.contains("E") ? Double.parseDouble(value) : Long.parseLong(value); } catch (RuntimeException exception) { throw error(); } }
    private void whitespace() { while (index < input.length() && Character.isWhitespace(input.charAt(index))) index++; }
    private boolean consume(char value) { if (index < input.length() && input.charAt(index) == value) { index++; return true; } return false; }
    private void require(char value) { if (!consume(value)) throw error(); }
    private IllegalArgumentException error() { return new IllegalArgumentException("JSON inválido no catálogo de aulas no índice " + index); }
}
