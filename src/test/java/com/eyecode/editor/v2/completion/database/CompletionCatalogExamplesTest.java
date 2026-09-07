package com.eyecode.editor.v2.completion.database;

import com.eyecode.editor.v2.completion.CompletionItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompletionCatalogExamplesTest {

    @Test
    void curatedMethodExamplesMatchTheirCatalogEntries() {
        assertExample(JavaLangSymbols.getAll(), "java.lang.System",
                "System.arraycopy(Object src, int srcPos, Object dest, int destPos, int length)",
                "int[] source = {1, 2, 3};\nint[] target = new int[3];\nSystem.arraycopy(source, 0, target, 0, source.length);");
        assertExample(JavaLangSymbols.getAll(), "java.lang.String", "String.charAt(int index)",
                "String text = \"EyeCode\";\nchar first = text.charAt(0);");
        assertExample(JavaLangSymbols.getAll(), "java.lang.String", "String.substring(int beginIndex)",
                "String text = \"EyeCode\";\nString part = text.substring(3);");
        assertExample(JavaLangSymbols.getAll(), "java.lang.String", "String.contains(CharSequence seq)",
                "boolean contains = \"EyeCode\".contains(\"Code\");");
        assertExample(JavaLangSymbols.getAll(), "java.lang.String", "String.startsWith(String prefix)",
                "boolean starts = \"EyeCode\".startsWith(\"Eye\");");
        assertExample(JavaLangSymbols.getAll(), "java.lang.String", "String.endsWith(String suffix)",
                "boolean ends = \"EyeCode\".endsWith(\"Code\");");
        assertExample(JavaLangSymbols.getAll(), "java.lang.String", "String.replace(char oldChar, char newChar)",
                "String result = \"EyeCode\".replace('E', 'A');");
        assertExample(JavaUtilSymbols.getAll(), "java.util.ArrayList", "ArrayList.add(E element)",
                "List<String> names = new ArrayList<>();\nnames.add(\"EyeCode\");");
        assertExample(JavaUtilSymbols.getAll(), "java.util.ArrayList", "ArrayList.remove(int index)",
                "List<String> names = new ArrayList<>(List.of(\"A\", \"B\"));\nnames.remove(0);");
        assertExample(JavaUtilSymbols.getAll(), "java.util.Optional", "Optional.orElse(T other)",
                "String value = optional.orElse(\"default\");");
        assertExample(JavaStreamSymbols.getAll(), "java.util.stream.Stream",
                "Stream.filter(Predicate<? super T> predicate)",
                "List<String> names = List.of(\"Ana\", \"Bob\", \"Alice\");\nList<String> result = names.stream()\n        .filter(name -> name.startsWith(\"A\"))\n        .toList();");
        assertExample(JavaStreamSymbols.getAll(), "java.util.stream.Stream",
                "Stream.map(Function<? super T, ? extends R> mapper)",
                "List<String> names = List.of(\"Ana\", \"Bob\");\nList<Integer> lengths = names.stream()\n        .map(String::length)\n        .toList();");
    }

    @Test
    void expandedCatalogExamplesMatchTheirCatalogEntries() {
        assertTypeExample(JavaLangSymbols.getAll(), "java.lang", "String",
                "String text = \"EyeCode\";\nint length = text.length();");
        assertTypeExample(JavaUtilSymbols.getAll(), "java.util", "Arrays",
                "int[] numbers = {3, 1, 2};\nArrays.sort(numbers);");
        assertTypeExample(JavaUtilSymbols.getAll(), "java.util", "ArrayList",
                "List<String> list = new ArrayList<>();\nlist.add(\"item\");");
        assertTypeExample(JavaUtilSymbols.getAll(), "java.util", "LinkedList",
                "LinkedList<String> queue = new LinkedList<>();\nqueue.add(\"first\");\nqueue.add(\"second\");");
        assertTypeExample(JavaUtilSymbols.getAll(), "java.util", "HashMap",
                "Map<String, Integer> map = new HashMap<>();\nmap.put(\"key\", 1);");
        assertTypeExample(JavaStreamSymbols.getAll(), "java.util.stream", "Stream",
                "Stream<String> names = Stream.of(\"Ana\", \"Bob\");\nnames.forEach(System.out::println);");

        assertExample(JavaUtilSymbols.getAll(), "java.util.Arrays", "Arrays.sort(int[] a)",
                "int[] numbers = {3, 1, 2};\nArrays.sort(numbers);");
        assertExample(JavaUtilSymbols.getAll(), "java.util.LinkedList", "LinkedList.addFirst(E e)",
                "LinkedList<String> queue = new LinkedList<>();\nqueue.addFirst(\"first\");");
        assertExample(JavaUtilSymbols.getAll(), "java.util.HashMap", "HashMap.put(K key, V value)",
                "Map<String, Integer> ages = new HashMap<>();\nages.put(\"Ana\", 25);");
        assertExample(JavaStreamSymbols.getAll(), "java.util.stream.Stream",
                "Stream.reduce(T identity, BinaryOperator<T> accumulator)",
                "int total = Stream.of(1, 2, 3).reduce(0, Integer::sum);");
    }

    private static void assertExample(List<CompletionItem> items, String owner, String signature, String example) {
        CompletionItem item = items.stream()
                .filter(candidate -> owner.equals(candidate.getOwner()))
                .filter(candidate -> signature.equals(candidate.getSignature()))
                .findFirst()
                .orElseThrow();

        assertEquals(example, item.getExample());
    }

    private static void assertTypeExample(List<CompletionItem> items, String owner, String label, String example) {
        CompletionItem item = items.stream()
                .filter(candidate -> owner.equals(candidate.getOwner()))
                .filter(candidate -> label.equals(candidate.getLabel()))
                .findFirst()
                .orElseThrow();

        assertEquals(example, item.getExample());
    }
}
