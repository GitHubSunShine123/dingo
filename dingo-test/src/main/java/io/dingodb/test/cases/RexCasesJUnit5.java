/*
 * Copyright 2021 DataCanvas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dingodb.test.cases;

import io.dingodb.exec.utils.DingoDateTimeUtils;
import io.dingodb.expr.runtime.utils.DateTimeUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.TimeZone;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Rex test case for junit5.
 * <p>
 * 1st arg: the SQL expression
 * 2nd arg: the Dingo expression
 * 3rd arg: the result
 */
public final class RexCasesJUnit5 implements ArgumentsProvider {
    private static @NonNull Arguments unixTimestampCase(@Nonnull String timestampStr) {
        Timestamp timestamp = DateTimeUtils.parseTimestamp(timestampStr);
        assert timestamp != null;
        return arguments(
            "unix_timestamp('" + timestampStr + "')",
            "unix_timestamp(TIMESTAMP('" + timestampStr.replace("/", "\\/") + "'))",
            timestamp.getTime() / 1000L
        );
    }

    private static @NonNull Arguments unixTimestampLiteralCase(@Nonnull String timestampStr) {
        Timestamp timestamp = DateTimeUtils.parseTimestamp(timestampStr);
        assert timestamp != null;
        long millis = timestamp.getTime();
        millis = millis + TimeZone.getDefault().getOffset(millis);
        return arguments(
            "unix_timestamp(TIMESTAMP '" + timestampStr + "')",
            "unix_timestamp(TIMESTAMP(" + DateTimeUtils.toSecond(millis) + "))",
            DateTimeUtils.toSecond(millis).longValue()
        );
    }

    private static @NonNull Arguments fromUnixTimeCase(@Nonnull String timestampStr) {
        Timestamp timestamp = DateTimeUtils.parseTimestamp(timestampStr);
        assert timestamp != null;
        return arguments(
            "from_unixtime(" + timestamp.getTime() / 1000L + ")",
            "from_unixtime(" + timestamp.getTime() / 1000L + ")",
            timestamp
        );
    }

    private static @NonNull Arguments timestampFormatNumericCase(long timestamp, String format) {
        return arguments(
            "timestamp_format(" + timestamp + ", " + "'" + format + "')",
            "timestamp_format(" + timestamp + ", " + "'" + format + "')",
            DingoDateTimeUtils.timestampFormat(new Timestamp(DateTimeUtils.fromSecond(timestamp)), format)
        );
    }

    @Override
    public @NonNull Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
            // Arithmetical
            arguments("'hello'", "'hello'", "hello"),
            arguments("1 + 1", "1 + 1", 2),
            arguments("1 + 2", "1 + 2", 3),
            arguments("1 + 2*3", "1 + 2*3", 7),
            arguments("1*(2 + 3)", "1*(2 + 3)", 5),
            arguments("1 + 100.1", "1 + 100.1", BigDecimal.valueOf(101.1)),
            arguments("1 + 100000000.2", "1 + 100000000.2", BigDecimal.valueOf(100000001.2)),
            // Number functions
            arguments("abs(15354.6651)", "abs(15354.6651)", BigDecimal.valueOf(15354.6651)),
            arguments("abs(-163.4)", "abs(-163.4)", BigDecimal.valueOf(163.4)),
            arguments(
                "abs(" + (Integer.MIN_VALUE + 1) + ")",
                "abs(" + (Integer.MIN_VALUE + 1) + ")",
                Integer.MAX_VALUE
            ),
            arguments(
                "abs(" + (Integer.MIN_VALUE - 1L) + ")",
                "abs(" + (Integer.MIN_VALUE - 1L) + ")",
                Integer.MAX_VALUE + 2L
            ),
            arguments(
                "abs(" + (Long.MIN_VALUE + 1L) + ")",
                "abs(" + (Long.MIN_VALUE + 1L) + ")",
                Long.MAX_VALUE
            ),
            arguments("ceiling(4533.66)", "ceil(4533.66)", BigDecimal.valueOf(4534)),
            arguments("ceil(-125.3)", "ceil(-125.3)", BigDecimal.valueOf(-125)),
            arguments("ceil(101.2)", "ceil(101.2)", BigDecimal.valueOf(102)),
            arguments("ceil(" + Integer.MIN_VALUE + ")", "ceil(" + Integer.MIN_VALUE + ")", Integer.MIN_VALUE),
            arguments("ceil(" + Integer.MAX_VALUE + ")", "ceil(" + Integer.MAX_VALUE + ")", Integer.MAX_VALUE),
            arguments("ceil(" + Long.MIN_VALUE + ")", "ceil(" + Long.MIN_VALUE + ")", Long.MIN_VALUE),
            arguments("ceil(" + Long.MAX_VALUE + ")", "ceil(" + Long.MAX_VALUE + ")", Long.MAX_VALUE),
            arguments("ceil(cast (3.6 as double))", "ceil(DOUBLE(3.6))", 4.0D),
            arguments("floor(125.6)", "floor(125.6)", BigDecimal.valueOf(125)),
            arguments("floor(-125.3)", "floor(-125.3)", BigDecimal.valueOf(-126)),
            arguments("floor(101.2)", "floor(101.2)", BigDecimal.valueOf(101)),
            arguments("floor(" + Integer.MIN_VALUE + ")", "floor(" + Integer.MIN_VALUE + ")", Integer.MIN_VALUE),
            arguments("floor(" + Integer.MAX_VALUE + ")", "floor(" + Integer.MAX_VALUE + ")", Integer.MAX_VALUE),
            arguments("floor(" + Long.MIN_VALUE + ")", "floor(" + Long.MIN_VALUE + ")", Long.MIN_VALUE),
            arguments("floor(" + Long.MAX_VALUE + ")", "floor(" + Long.MAX_VALUE + ")", Long.MAX_VALUE),
            arguments("floor(cast (3.6 as double))", "floor(DOUBLE(3.6))", 3.0D),
            arguments("format(100.21, 1)", "format(100.21, 1)", "100.2"),
            arguments("format(99.00000, 2)", "format(99.00000, 2)", "99.00"),
            arguments("format(1220.532, 0)", "format(1220.532, 0)", "1221"),
            arguments("format(18, 2)", "format(18, 2)", "18.00"),
            arguments("format(15354.6651, 1.6)", "format(15354.6651, 1.6)", "15354.67"),
            arguments("mod(4, 3)", "mod(4, 3)", 1),
            arguments("mod(-4, 3)", "mod(-4, 3)", -1),
            arguments("mod(4, -3)", "mod(4, -3)", 1),
            arguments("mod(-4, -3)", "mod(-4, -3)", -1),
            arguments("mod(99, 2)", "mod(99, 2)", 1),
            arguments("mod(-107, 2)", "mod(-107, 2)", -1),
            arguments("mod(5.0, 2.5)", "mod(5.0, 2.5)", BigDecimal.valueOf(0.0)),
            arguments("mod(5.1, 2.5)", "mod(5.1, 2.5)", BigDecimal.valueOf(0.1)),
            arguments("mod(4, 0)", "mod(4, 0)", null),
            arguments("pow(2, 3)", "pow(2, 3)", BigDecimal.valueOf(8)),
            arguments("pow(3, 3)", "pow(3, 3)", BigDecimal.valueOf(27)),
            arguments("pow(-3.1, 3)", "pow(-3.1, 3)", BigDecimal.valueOf(-29.791)),
            arguments("pow('10', -2)", "pow(DECIMAL('10'), -2)", BigDecimal.valueOf(0.01)),
            arguments("round(123, -2)", "round(123, -2)", 100),
            arguments("round(12.677, 2)", "round(12.677, 2)", BigDecimal.valueOf(12.68)),
            arguments("round(195334.12, -4)", "round(195334.12, -4)", BigDecimal.valueOf(200000)),
            arguments("round(-155.586, -2)", "round(-155.586, -2)", BigDecimal.valueOf(-200)),
            arguments("round(101, -2)", "round(101, -2)", 100),
            arguments("round(101.0, -2)", "round(101.0, -2)", BigDecimal.valueOf(100)),
            arguments("round(105, -2)", "round(105, -2)", 100),
            arguments("round(105, -1)", "round(105, -1)", 110),
            arguments("round(105, '-2')", "round(105, LONG('-2'))", 100),
            arguments("round(10)", "round(10)", 10),
            arguments("round(cast (3.6 as double), 0)", "round(DOUBLE(3.6), 0)", 4.0D),
            arguments("round(cast (3.4 as double))", "round(DOUBLE(3.4))", 3.0D),
            // String functions
            arguments("'AA' || 'BB' || 'CC'", "concat(concat('AA', 'BB'), 'CC')", "AABBCC"),
            arguments("'abc' || null", "concat('abc', NULL)", null),
            arguments("concat(concat('AA', 'BB'), 'CC')", "concat(concat('AA', 'BB'), 'CC')", "AABBCC"),
            arguments("left('ABC', 2)", "left('ABC', 2)", "AB"),
            arguments("left('ABC-DEF', 3)", "left('ABC-DEF', 3)", "ABC"),
            arguments("left('ABC-DEF', 10)", "left('ABC-DEF', 10)", "ABC-DEF"),
            arguments("left('ABC-DEF', -3)", "left('ABC-DEF', -3)", ""),
            arguments("right('ABC', 1)", "right('ABC', 1)", "C"),
            arguments("right('ABC', 1.5)", "right('ABC', 1.5)", "BC"),
            arguments("ltrim(' AAA ')", "ltrim(' AAA ')", "AAA "),
            arguments("rtrim(' AAA ')", "rtrim(' AAA ')", " AAA"),
            arguments("'a'||rtrim(' ')||'b'", "concat(concat('a', rtrim(' ')), 'b')", "ab"),
            arguments("locate('\\c', 'ab\\cde')", "locate('\\\\c', 'ab\\\\cde')", 3),
            arguments("locate('C', 'ABCd')", "locate('C', 'ABCd')", 3),
            arguments("locate('abc', '')", "locate('abc', '')", 0),
            arguments("locate('', '')", "locate('', '')", 1),
            arguments("mid('ABC', 1, 2)", "mid('ABC', 1, 2)", "AB"),
            arguments("mid('ABC', 2, 3)", "mid('ABC', 2, 3)", "BC"),
            arguments("mid('ABCDEFG', -5, 3)", "mid('ABCDEFG', -5, 3)", "CDE"),
            arguments("mid('ABCDEFG', 1, -5)", "mid('ABCDEFG', 1, -5)", ""),
            arguments("mid('ABCDEFG', 2)", "mid('ABCDEFG', 2)", "BCDEFG"),
            arguments("mid('ABCDEFG', 2.5, 3)", "mid('ABCDEFG', 2.5, 3)", "CDE"),
            arguments("mid('ABCDEFG', 2, 3.5)", "mid('ABCDEFG', 2, 3.5)", "BCDE"),
            arguments("repeat('ABC', 3)", "repeat('ABC', 3)", "ABCABCABC"),
            arguments("repeat('ABC', 2.1)", "repeat('ABC', 2.1)", "ABCABC"),
            arguments("replace('MongoDB', 'Mongo', 'Dingo')", "replace('MongoDB', 'Mongo', 'Dingo')", "DingoDB"),
            arguments("replace(null, 'a', 'b')", "replace(NULL, 'a', 'b')", null),
            arguments("replace('MongoDB', null, 'b')", "replace('MongoDB', NULL, 'b')", null),
            arguments(
                "replace('I love $name', '$name', 'Lucia')",
                "replace('I love $name', '$name', 'Lucia')",
                "I love Lucia"
            ),
            arguments("reverse('ABC')", "reverse('ABC')", "CBA"),
            arguments("substring('DingoDatabase', 1, 5)", "substring('DingoDatabase', 1, 5)", "Dingo"),
            arguments("substring('DingoDatabase', 1, 100)", "substring('DingoDatabase', 1, 100)", "DingoDatabase"),
            arguments("substring('DingoDatabase', 2, 2.5)", "substring('DingoDatabase', 2, 2.5)", "ing"),
            arguments("substring('DingoDatabase', 2, cast (2.5 as int))", "substring('DingoDatabase', 2, INT(2.5))", "ing"),
            arguments("substring('DingoDatabase', 2, -3)", "substring('DingoDatabase', 2, -3)", ""),
            arguments("substring('DingoDatabase', -4, 4)", "substring('DingoDatabase', -4, 4)", "base"),
            arguments("substring('abc-def', 1, 8)", "substring('abc-def', 1, 8)", "abc-def"),
            arguments("substring('', 1, 3)", "substring('', 1, 3)", ""),
            arguments("substring(null, 1, 1)", "substring(NULL, 1, 1)", null),
            arguments("trim(' AAAAA  ')", "trim('BOTH', ' ', ' AAAAA  ')", "AAAAA"),
            arguments("trim(BOTH 'A' from 'ABBA')", "trim('BOTH', 'A', 'ABBA')", "BB"),
            arguments("trim('  ')", "trim('BOTH', ' ', '  ')", ""),
            arguments("trim(' A ')", "trim('BOTH', ' ', ' A ')", "A"),
            arguments("trim(123 from 1234123)", "trim('BOTH', STRING(123), STRING(1234123))", "4"),
            arguments("trim(LEADING 'A' from 'ABBA')", "trim('LEADING', 'A', 'ABBA')", "BBA"),
            arguments("trim(TRAILING 'A' from 'ABBA')", "trim('TRAILING', 'A', 'ABBA')", "ABB"),
            arguments("upper('aaa')", "upper('aaa')", "AAA"),
            arguments("upper('HeLlO')", "upper('HeLlO')", "HELLO"),
            arguments("ucase('aaa')", "upper('aaa')", "AAA"),
            arguments("lower('aaA')", "lower('aaA')", "aaa"),
            arguments("lower('HeLlO')", "lower('HeLlO')", "hello"),
            arguments("lcase('Aaa')", "lower('Aaa')", "aaa"),
            // Date & time functions
            arguments("CAST('1970.1.2' AS DATE)", "DATE('1970.1.2')", "1970-01-02"),
            arguments(
                "CAST('2020-11-01 01:01:01' AS TIMESTAMP)",
                "TIMESTAMP('2020-11-01 01:01:01')",
                DateTimeUtils.parseTimestamp("2020-11-01 01:01:01")
            ),
            arguments(
                "* from (SELECT CAST('2020-11-01 01:01:01' AS TIMESTAMP))",
                "TIMESTAMP('2020-11-01 01:01:01')",
                Timestamp.valueOf("2020-11-01 01:01:01")
            ),
            unixTimestampCase("1980-11-12 23:25:12"),
            unixTimestampCase("2022-04-14 00:00:00"),
            unixTimestampCase("2022/04/14 00:00:00"),
            unixTimestampCase("2022-05-15 00:14:01"),
            unixTimestampLiteralCase("1980-11-12 23:25:12"),
            unixTimestampLiteralCase("2022-04-14 00:00:00"),
            arguments("unix_timestamp(0)", "unix_timestamp(0)", 0L),
            arguments("unix_timestamp(16525448410)", "unix_timestamp(16525448410)", 16525448410L),
            fromUnixTimeCase("1980-11-12 23:25:12"),
            fromUnixTimeCase("2022-04-14 00:00:00"),
            arguments("from_unixtime(0)", "from_unixtime(0)", new Timestamp(0)),
            arguments("from_unixtime(1649770110)", "from_unixtime(1649770110)", new Timestamp(1649770110000L)),
            arguments("date_format('2022/7/2')", "date_format(DATE('2022\\/7\\/2'))", "2022-07-02"),
            arguments("date_format('', '%Y-%m-%d')", "date_format(DATE(''), '%Y-%m-%d')", null),
            arguments(
                "date_format('1999-01-01', '%Y-%m-%d')",
                "date_format(DATE('1999-01-01'), '%Y-%m-%d')",
                "1999-01-01"
            ),
            arguments(
                "date_format('1999/01/01', '%Y/%m/%d')",
                "date_format(DATE('1999\\/01\\/01'), '%Y\\/%m\\/%d')",
                "1999/01/01"
            ),
            arguments(
                "date_format('1999.01.01', '%Y.%m.%d')",
                "date_format(DATE('1999.01.01'), '%Y.%m.%d')",
                "1999.01.01"
            ),
            arguments(
                "date_format('1999-01-01', '%Y-%m-%d %T')",
                "date_format(DATE('1999-01-01'), '%Y-%m-%d %T')",
                "1999-01-01 00:00:00"
            ),
            arguments(
                "date_format('1999-01-01', '%Y year %m month %d day')",
                "date_format(DATE('1999-01-01'), '%Y year %m month %d day')",
                "1999 year 01 month 01 day"
            ),
            arguments(
                "date_format('2022-04-1', '%Y-%m-%d')",
                "date_format(DATE('2022-04-1'), '%Y-%m-%d')",
                "2022-04-01"
            ),
            arguments(
                "date_format('2022-04-1', '%Y-%m-%d %A')",
                "date_format(DATE('2022-04-1'), '%Y-%m-%d %A')",
                "2022-04-01 A"
            ),
            arguments(
                "date_format('20220413', 'Year:%Y Month:%m Day:%d')",
                "date_format(DATE('20220413'), 'Year:%Y Month:%m Day:%d')",
                "Year:2022 Month:04 Day:13"
            ),
            arguments(
                "date_format(cast ('1980-2-3' as date), '%Y:%m:%d')",
                "date_format(DATE('1980-2-3'), '%Y:%m:%d')",
                "1980:02:03"
            ),
            arguments(
                "DATE_FORMAT(CAST('2020/11/3' AS DATE), '%Y year, %m month %d day')",
                "date_format(DATE('2020\\/11\\/3'), '%Y year, %m month %d day')",
                "2020 year, 11 month 03 day"
            ),
            arguments(
                "DATE_FORMAT(CAST('2020.11.30' AS DATE), '%Y year, %m month')",
                "date_format(DATE('2020.11.30'), '%Y year, %m month')",
                "2020 year, 11 month"
            ),
            arguments("time_format('111213', '%H-%i.%s')", "time_format(TIME('111213'), '%H-%i.%s')", "11-12.13"),
            arguments("time_format('11:2:3', '%H.%i.%s')", "time_format(TIME('11:2:3'), '%H.%i.%s')", "11.02.03"),
            arguments("time_format('110203', '%H%i%S')", "time_format(TIME('110203'), '%H%i%S')", "110203"),
            arguments("time_format('083026', '%H.%i.%s')", "time_format(TIME('083026'), '%H.%i.%s')", "08.30.26"),
            arguments("time_format('000006', '%H.%i.%s')", "time_format(TIME('000006'), '%H.%i.%s')", "00.00.06"),
            arguments("time_format('180000', '%H.%i.%s')", "time_format(TIME('180000'), '%H.%i.%s')", "18.00.00"),
            arguments("time_format('23:59:59', '%H.%i.%s')", "time_format(TIME('23:59:59'), '%H.%i.%s')", "23.59.59"),
            arguments(
                "time_format('19:00:28.331', '%H.%i.%s.%f')",
                "time_format(TIME('19:00:28.331'), '%H.%i.%s.%f')",
                "19.00.28.331"
            ),
            arguments(
                "time_format('235959')",
                "time_format(TIME('235959'))",
                "23:59:59"
            ),
            arguments(
                "time_format(cast ('23:11:25' as time), '%H-%i-%s')",
                "time_format(TIME('23:11:25'), '%H-%i-%s')",
                "23-11-25"
            ),
            arguments(
                "timestamp_format('1999/1/01 01:01:01', '%Y/%m/%d %T')",
                "timestamp_format(TIMESTAMP('1999\\/1\\/01 01:01:01'), '%Y\\/%m\\/%d %T')",
                "1999/01/01 01:01:01"
            ),
            arguments(
                "timestamp_format('1999.01.01 01:01:01', '%Y.%m.%d %T')",
                "timestamp_format(TIMESTAMP('1999.01.01 01:01:01'), '%Y.%m.%d %T')",
                "1999.01.01 01:01:01"
            ),
            arguments(
                "timestamp_format('19990101010101', '%Y%m%d %T')",
                "timestamp_format(TIMESTAMP('19990101010101'), '%Y%m%d %T')",
                "19990101 01:01:01"
            ),
            arguments(
                "timestamp_format('20220413103706', '%Y/%m/%d %H:%i:%S')",
                "timestamp_format(TIMESTAMP('20220413103706'), '%Y\\/%m\\/%d %H:%i:%S')",
                "2022/04/13 10:37:06"
            ),
            arguments(
                "timestamp_format('1999-01-01 01:01:01.22', '%Y/%m/%d %T.%f')",
                "timestamp_format(TIMESTAMP('1999-01-01 01:01:01.22'), '%Y\\/%m\\/%d %T.%f')",
                "1999/01/01 01:01:01.220"
            ),
            arguments(
                "timestamp_format('2022-04-13 10:37:26', '%Ss')",
                "timestamp_format(TIMESTAMP('2022-04-13 10:37:26'), '%Ss')",
                "26s"
            ),
            arguments(
                "timestamp_format('1999-01-01 10:37:26', '%Y year %m month %d day and %s seconds')",
                "timestamp_format(TIMESTAMP('1999-01-01 10:37:26'), '%Y year %m month %d day and %s seconds')",
                "1999 year 01 month 01 day and 26 seconds"
            ),
            arguments(
                "timestamp_format('2022-04-13 10:37:26', '%m mon %d day %Y year, %H hour %i min %S sec')",
                "timestamp_format(TIMESTAMP('2022-04-13 10:37:26'), '%m mon %d day %Y year, %H hour %i min %S sec')",
                "04 mon 13 day 2022 year, 10 hour 37 min 26 sec"
            ),
            arguments(
                "timestamp_format('2022-04-13 10:37:36', '%H:%i:%S')",
                "timestamp_format(TIMESTAMP('2022-04-13 10:37:36'), '%H:%i:%S')",
                "10:37:36"
            ),
            arguments(
                "timestamp_format('20220413103726', 'Year:%Y Month:%m Day:%d')",
                "timestamp_format(TIMESTAMP('20220413103726'), 'Year:%Y Month:%m Day:%d')",
                "Year:2022 Month:04 Day:13"
            ),
            arguments(
                "timestamp_format('2022/07/22 12:00:00')",
                "timestamp_format(TIMESTAMP('2022\\/07\\/22 12:00:00'))",
                "2022-07-22 12:00:00"
            ),
            arguments(
                "timestamp_format(cast ('1980-2-3 23:11:25' as timestamp), 'Date: %Y%m%d Time: %T')",
                "timestamp_format(TIMESTAMP('1980-2-3 23:11:25'), 'Date: %Y%m%d Time: %T')",
                "Date: 19800203 Time: 23:11:25"
            ),
            arguments(
                "timestamp_format(cast ('1980-2-3 23:11:25' as timestamp), '%Y%m%d %T')",
                "timestamp_format(TIMESTAMP('1980-2-3 23:11:25'), '%Y%m%d %T')",
                "19800203 23:11:25"
            ),
            arguments(
                "TIMESTAMP_FORMAT(CAST('2020.11.30 9:1:1' AS TIMESTAMP), '%Y year, %m month')",
                "timestamp_format(TIMESTAMP('2020.11.30 9:1:1'), '%Y year, %m month')",
                "2020 year, 11 month"
            ),
            arguments(
                "TIMESTAMP_FORMAT(CAST('2020.11.30 01:1:01' AS TIMESTAMP), '%Y year, %m month')",
                "timestamp_format(TIMESTAMP('2020.11.30 01:1:01'), '%Y year, %m month')",
                "2020 year, 11 month"
            ),
            arguments(
                "TIMESTAMP_FORMAT(CAST('2020/11/30 01:1:01' AS TIMESTAMP), '%Y year, %m month')",
                "timestamp_format(TIMESTAMP('2020\\/11\\/30 01:1:01'), '%Y year, %m month')",
                "2020 year, 11 month"
            ),
            timestampFormatNumericCase(1668477663, "%Y-%m-%d %H:%i:%S"),
            timestampFormatNumericCase(0, "%Y%m%d %H%i%S"),
            arguments("datediff('2007-12-29', '2007-12-30')", "datediff(DATE('2007-12-29'), DATE('2007-12-30'))", -1L),
            arguments("datediff('2007-12-29', '2007-12-30')", "datediff(DATE('2007-12-29'), DATE('2007-12-30'))", -1L),
            arguments("datediff('2022-04-14', '2022-05-31')", "datediff(DATE('2022-04-14'), DATE('2022-05-31'))", -47L),
            arguments("datediff('2022-04-30', '2022-05-01')", "datediff(DATE('2022-04-30'), DATE('2022-05-01'))", -1L),
            arguments("datediff('2022-04-30', '2022-05-31')", "datediff(DATE('2022-04-30'), DATE('2022-05-31'))", -31L),
            arguments("datediff('2022-05-31', '2022-04-13')", "datediff(DATE('2022-05-31'), DATE('2022-04-13'))", 48L),
            arguments("datediff('2022-05-31', '2022-05-01')", "datediff(DATE('2022-05-31'), DATE('2022-05-01'))", 30L),
            arguments("datediff('', '')", "datediff(DATE(''), DATE(''))", null),
            // Array functions
            arguments("array[1, 2, 3][2]", "item(LIST(1, 2, 3), 2)", 2)
        );
    }
}
