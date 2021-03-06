import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* У нас есть сайт http://pogoda.spb.ru/
 * Будем выводить информацию с сайта столбцами.
 * Т.е. парсим сайт, забираем себе некоторые данные.
 * "Дата Явление Температура Давление Влажность Ветер"
 * Для этого разберемся на примере ниже, как это делать.
 *
 * Конечно логику писать в main методе не красиво, но для наглядности примера
 * можно оставить.
 *
 * Здесь также нужно доработать красивый вывод данных в консоль.*/

public class Parser {

    /*
        Метод getPage(), который забирает всю страницу с сайта и возвращает
        некую сущьность, которая называется Document.
        Это что то типа строки, но строки с нашим html кодом.
        Document учитывает вложенности в html коде, например один div
        содержит в себе еще один div, и помагает удобно работать с
        html страницей. Т.е. библиотека Jsoup помагает нам искать
        на странице нужные нам элементы.
    */
    private static Document getPage() throws IOException {

        //указываем url откуда качаем страницу

        String url = "http://pogoda.spb.ru/";
/*
        И теперь говорим, что мы хотим получить Document
        с именем page. Используем класс Jsoup и его метод
        parse(). Метод parse() парсит страницу и формирует объект Document.
        Передаем в аргументе ему объект URL с нашим url.
        Также указываем количество милисекунд, которое
        мы можем ждать ответа от сервера. Не будем же мы ждать вечно.
        Исключения от URL пробросим наверх throws IOException и не будем
        пока их обрабатывать.
*/
        Document page = Jsoup.parse(new URL(url), 3000);
        return page;
    }

    /* Напишем регулярный метод getDateFromString для вырезания данных из строки.
     * Есть строка -> "05.09 Четверг погода сегодня"
     * А нам нужно получить только: 05.09
     *
     * Чтобы воспользоваться регулярными выражениями в Java нужно создать Pattern.
     * Pattern - это механизм чтобы искать нужную нам информацию в тексте.
     * В Pattern мы будем записывать наше само регулярное выражение, потом из этого Pattern будем
     * создавать некий matcher (это та штука которая ищет (матчит) нужную инфу в тексте).
     * Т.е. в Паттерне будет храниться шаблон по которому матчер будет искать информацию.
     *
     * Pattern.compile("") - в скобках стоит регулярное выражение.
     * Нас интересует чтоб сначало стояли две цифры, потом точка и потом опять две цифры.
     * Чтобы идентифицировать символ, в регулярных выражениях есть "\d" и он говорит,
     * что это символ (т.е. символьный знак).
     * \d{2}\.\d{2} - говорим: идет два символьных знака, потом . и потом опять два символьных знака.
     * Это и есть наше выражение.
     * Внимание в коде будет по два обратных слеша -> \\ Это потому, что регуляное выражение
     * храниться в строке.
     * Для более детального ознакомления, читай книгу.
     * */
    private static Pattern pattern = Pattern.compile("\\d{2}\\.\\d{2}");

    private static String getDateFromString(String stringData) throws Exception {

        /*Создадим Матчер
          говорим: паттерн дай нам матчер и заматч его в строке stringData
        */
        Matcher matcher = pattern.matcher(stringData);

        //Теперь хотим получить значения.
        if (matcher.find()) {   //если матчер что то нашел, то

            return matcher.group();   //матчер группируй найденное и верни его

        }
        //если ничего не нашел, то бросим исключение. Оно же выбросится в сигнатуре метода
        throw new Exception("Can't extract date from string!");

    }

    /* Создаем метод printPartValues, чтоб выводить по 4 значения values.
     * Он будет сразу печатать на экран.
     * Он принимает эти values в параметр и index с какого печатать.
     * */
    private static int printPartValues(Elements values, int index) {

        int iterationCount = 4;
        /* index==0 когда мы на первом дне. Количество values может меняться
         * в зависимомти от времения суток: утро-день-вечер-ночь -> день-вечер-ночь -> вечер-ночь
         * В блоке if  проверяем Первый день, какая его часть */
        if (index == 0) {
            Element valueLn = values.get(3);

            //сделаем флаг
            //если в стоке есть слово Утро то итерации будет всего лишь 3
            boolean isMorning = valueLn.text().contains("Утро");

            //если в стоке есть слово День то итерации будет всего лишь 2 и т.д.
            boolean isAfternoon = valueLn.text().contains("День");
            boolean isEvening = valueLn.text().contains("Вечер");
            boolean isTonight = valueLn.text().contains("Ночь");

            //переменная iterationCount будет по-умолчанию =4, а если есть "Утро" то =3
            //А если есть слово "День" то iterationCount=2,
            //и далее "Вечер" - iterationCount=1, "Ночь" -> =0
            //изначально iterationCount = 4;
            //И мы проверяем для первого случая, что это или Утро или
            //День или Вечер или Ночь, а последующие дни выводятся полностью.

            if (isMorning) {
                iterationCount = 3;
            } else if (isAfternoon) {
                iterationCount = 2;
            } else if (isEvening) {
                iterationCount = 1;
            } else if (isTonight) {
                iterationCount = 0;
            }

        }
        for (int i = 0; i < iterationCount; i++) {

            /* Для начала получим все tr valign=top элементы и нам нужен
             * нужный tr по индексу index
             * */
            //забираем элемент valueLine по индексу index=(0 + i)
            Element valueLine = values.get(index + i); //из values возьмем по индексу и сохраним в valueLine

            /*теперь из valueLine отдай мне все td*/
            for (Element td : valueLine.select("td")) {
                //выводим только текс из td
                System.out.print(td.text() + "    ");
            }
            System.out.println();
        }
        return iterationCount; //возвращаем количество итераций
    }

    public static void main(String[] args) throws Exception {

        //System.out.println(getPage()); - можно посмотреть на всю страницу

        //Запросим нашу страницу
        Document page = getPage();

        //Получаем из страницы блок отвечающий за погодные данные.
        //Element - элемент нашей страницы.
        /*css query language - позволяет делать удобные запросы к странице и
         и с помощью этих запросов доставать со странице нужные нам данные.
         Сейчас нам нужен блок отвечающий за данные погоды, а не вся страница.
         В частности вот этот блок:
         <table width="100%" cellspacing="0" cellpadding="4" class="wt">

         Далее мы скажем:"page я хочу с тебя достать (select - я выбираю) все table
         (указываем тип блока, который хотим достатать) с вот этим уникальным идентификатором
         в квадратных скобках [class=wt]". Т.е. мы запросили с этой страницы
         все table с классом class=wt. Но дело в том, что таких элементов может быть много
         на странице и поэтому попросим отдать нам первый .first()

         И мы получим: <table width="100%" cellspacing="0" cellpadding="4" class="wt">...</table>
         И останется эти данные разделить и вывести на экран.

         Так же напомню, что мы хотим забрать данные о погоде с сайта целиком и вывести у себя в таблицу.
         */
        Element tableWth = page.select("table[class=wt]").first();

        //System.out.println(tableWth); //все данные выведенные с помощью html, не красивый вид

        /* Видим, что наша таблица состоит из <table><body><tr>..</tr></body></table>
         * И нам нужно выбрать те tr у которых class=wth. Т.е. это шапка для отдельной даты.
         * Создаем Elements с именем names и говорим, что мы хотим из нашей таблицы
         * только те tr, у которых class=wth*/
        Elements names = tableWth.select("tr[class=wth]");

        /* Теперь обращаемся к таблице и говорим:"отдай мне все tr у которых
         * идентификатор valign=top. Это подстроки "утро, день, вечер, ночь"*/
        Elements values = tableWth.select("tr[valign=top]");

        /* Создадим index чтоб знать в какой строке находимся.
         * У нас строк от 0 до 19, т.е. 20 значений values*/
        int index = 0; //говорит на каком значении values мы находимся

        /*Пройдемся циклом по names чтобы их вывести*/
        for (Element name : names) {

            /* Обращаемся к ячейке таблицы <th id="dt"> и забираем данные даты
             * И все что лежит в th блоке будет записано в переменную date*/
            String dateString = name.select("th[id=dt]").text();

            /* Чтобы верезать только дату отсюда, можно воспользоваться регулярными выражениями.
             * Регулярные выражения позволяют из любой строки выделить только
             * нужную нам информацию.
             * Статический метод для регулярных выражений смотри выше.*/
            String date = getDateFromString(dateString); //получим из строки только дату. Есть иксепшн, кидаем его дальше.

            System.out.println(date + "\tЯвление\t\tТемпература\t\tДавл\t\tВлажность\t\tВетер");

            /*Осталось поместить данные tr valign=top между этими строками:
              05.09   Явление  Температура  Давл  Влажность  Ветер
              06.09   Явление  Температура  Давл  Влажность  Ветер
              07.09   Явление  Температура  Давл  Влажность  Ветер
              08.09   Явление  Температура  Давл  Влажность  Ветер
              09.09   Явление  Температура  Давл  Влажность  Ветер

              tr valign=top лежит в values и нам надо выводить не все values,а по 4
              утро-день-вечер-ночь*/

            int iterationCount = printPartValues(values, index);

            /* Точка старта, с которой нужно печатать, увеличится на столько
             * сколько было напечатано в этом блоке элементов*/
            index = index + iterationCount;
        }
    }
}
