package ru.kuranov;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    // путь к файлу входящих наименований товаров
    private static final String INPUT_FILE = "input.txt";

    // путь к результирующему файлу сопоставленных наименований товаров
    private static final String OUTPUT_FILE = "output.txt";

    // паттерн для разделения входящих наименований товаров на группы
    private static final String BLOCK_DELIMITER = "^\\d+$";

    // паттерн выделения основ слов
    private static final String PATTERN = "([ёеыаоэяиюу])+$";

    // разделитель слов в строках
    private static final String SPLIT = "\\s";

    // разделитель наименований товаров результирующего файла
    private static final String DELIMITER = ":";

    // разделитель для наименований товаров для которых не нашлось аналога
    private static final String EMPTY_MATCH = ":?";

    // разделитель строк результирующего файла
    private static final String OUTPUT_STRING_DELIMITER = "\n";


    public static void main(String[] args) throws IOException {

        // читаем данные из файла входящих наименований товаров
        List<String> strings = getStrings();

        // сопоставляем товары
        List<String> outputResult = matchingStrings(strings);

        // записываем результат сопоставления в файл
        writeResult(outputResult);
    }

    // читаем данные из файла входящих наименований товаров
    private static List<String> getStrings() throws FileNotFoundException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(INPUT_FILE));
        return bufferedReader.lines().collect(Collectors.toList());
    }


    // сопоставляем товары
    private static List<String> matchingStrings(List<String> strings) {

        // определяем местоположение блоков строк для последующего разделения
        int divideIndex = 0;
        for (int i = strings.size() - 1; i >= 0; i--) {
            if (strings.get(i).matches(BLOCK_DELIMITER)) {
                divideIndex = i;
                break;
            }
        }

        // разделяем входящие строки на 2 блока для сопоставления,
        // String -  наименование товара,
        // Integer - числовой индекс количества результативных сопоставлений товара
        Map<String, Integer> blockA = strings.subList(1, divideIndex).stream()
                .distinct()
                .collect(Collectors.toMap(s -> s, s -> 0));
        Map<String, Integer> blockB = strings.subList(divideIndex + 1, strings.size()).stream()
                .distinct()
                .collect(Collectors.toMap(s -> s, s -> 0));

        // определяем динамический массив для аккумуляции результатов сопоставления
        List<String> outputResult = new ArrayList<>();

        // итерация по первому блоку
        for (Map.Entry<String, Integer> stringA : blockA.entrySet()) {

            // итерация по второму блоку
            for (Map.Entry<String, Integer> stringB : blockB.entrySet()) {

                // если в блоках всего по одной строке, то считаем их сопоставленными
                if (blockA.size() == 1 && blockB.size() == 1) {
                    outputResult.add(stringA.getKey() + DELIMITER + stringB.getKey());
                    return outputResult;
                }

                // переменная для возврата из цикла на уровень выше
                boolean isGoToUpCycle = false;

                // разделяем строки на слова для сопоставления
                List<String> splitWordsA = Arrays.stream(stringA.getKey().split(SPLIT)).collect(Collectors.toList());
                List<String> splitWordsB = Arrays.stream(stringB.getKey().split(SPLIT)).collect(Collectors.toList());

                // итерация по словам из строк первого блока
                for (String wordA : splitWordsA) {
                    if (isGoToUpCycle) {
                        break;
                    }

                    // обрезаем окончания слов для сопоставления основ слов
                    String matchA = wordA.replaceAll(PATTERN, "");

                    // итерация по словам из строк второго блока
                    for (String wordB : splitWordsB) {
                        if (isGoToUpCycle) {
                            break;
                        }

                        // обрезаем окончания слов для сопоставления основ слов
                        String matchB = wordB.replaceAll(PATTERN, "");

                        // сопоставление основ слов
                        if (matchA.equalsIgnoreCase(matchB)) {

                            // запись сопоставленных значений в динамический массив
                            outputResult.add(stringA.getKey() + DELIMITER + stringB.getKey());

                            // увеличиваем индекс количества результативных сопоставлений товара
                            stringA.setValue(stringA.getValue() + 1);
                            stringB.setValue(stringB.getValue() + 1);

                            // выходим из цикла на уровень выше
                            isGoToUpCycle = true;
                        }
                    }
                }
            }

        }

        // обходим первый блок для поиска товара, которому не нашлось сопоставления,
        // если находим, то заносим в диманический массив с вопросительным знаком
        blockA.forEach((key, value) -> {
            if (value == 0) {
                outputResult.add(key + EMPTY_MATCH);
            }
        });


        // обходим второй блок для поиска товара, которому не нашлось сопоставления,
        // если находим, то заносим в диманический массив с вопросительным знаком
        blockB.forEach((key, value) -> {
            if (value == 0) {
                outputResult.add(key + EMPTY_MATCH);
            }
        });

        return outputResult;
    }


    // записываем результат сопоставления в файл
    private static void writeResult(List<String> out) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(OUTPUT_FILE));
        bufferedWriter.write(String.join(OUTPUT_STRING_DELIMITER, out));
        bufferedWriter.flush();
    }
}
