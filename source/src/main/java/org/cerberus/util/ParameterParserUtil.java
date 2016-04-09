/*
 * Cerberus  Copyright (C) 2013  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 * Class used in jsp or servlet in order to centralize all the parameter
 * parsing.
 *
 * @author bdumont
 */
public final class ParameterParserUtil {

    public static final String DEFAULT_BOOLEAN_TRUE_VALUE = "Y";
    public static final String DEFAULT_BOOLEAN_FALSE_VALUE = "N";

    private static final PolicyFactory POLICY = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
    private static final String DEFAULT_SQL_STRING_VALUE = "";
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("&?([^=]+)=([^&]+)");

    /**
     * To avoid instanciation of utility.
     */
    private ParameterParserUtil() {
    }

    /**
     * @param inString
     * @return "%" if the parameter is empty. This can be used before calling a
     * DAO where LIKE are used.
     */
    public static String wildcardIfEmpty(String inString) {
        if ((inString == null) || (inString.equalsIgnoreCase(""))) {
            return "%";
        }
        return inString;
    }

    /**
     *
     * @param column
     * @param inString
     * @return
     */
    public static String wildcardOrIsNullIfEmpty(String column, String inString) {
        StringBuilder sb = new StringBuilder();
        if ((inString == null) || (inString.equalsIgnoreCase(""))) {
            sb.append("'%' or ").append(column).append(" is null");
            return sb.toString();
        }
        sb.append("'").append(inString).append("'");
        return sb.toString();
    }

    /**
     *
     * @param column
     * @param inInt
     * @return
     */
    public static String wildcardOrIsNullIfMinusOne(String column, int inInt) {
        StringBuilder sb = new StringBuilder();

        if (inInt != -1) {
            sb.append("'").append(inInt).append("'");
        } else {
            sb.append("'%' or ").append(column).append(" is null");
        }

        return sb.toString();
    }

    /**
     * @param inParam
     * @param defaultVal
     * @return an empty string if the inParam is empty or null. It returns
     * inParam if OK.
     */
    public static String parseStringParam(String inParam, String defaultVal) {
        if (inParam != null && inParam.compareTo("") != 0) {
            return inParam;
        }
        return defaultVal;
    }

    /**
     * Parses and decodes and Sanitize the given inParam
     *
     * @see #parseStringParam(String, String)
     *
     * @param inParam
     * @param defaultVal
     * @param charset
     * @return
     */
    public static String parseStringParamAndDecodeAndSanitize(String inParam, String defaultVal, String charset) {
        if (inParam == null) {
            return defaultVal;
        }

        try {
            return parseStringParam(POLICY.sanitize(URLDecoder.decode(inParam, charset)), defaultVal);
        } catch (UnsupportedEncodingException e) {
            return defaultVal;
        }
    }

    /**
     * Parses and decodes and Sanitize the given inParam
     *
     * @see #parseStringParam(String, String)
     *
     * @param inParam
     * @param defaultVal
     * @param charset
     * @return
     */
    public static String parseStringParamAndDecode(String inParam, String defaultVal, String charset) {
        if (inParam == null) {
            return defaultVal;
        }

        try {
            return parseStringParam(URLDecoder.decode(inParam, charset), defaultVal);
        } catch (UnsupportedEncodingException e) {
            return defaultVal;
        }
    }

    /**
     *
     * @param inParam
     * @param defaultValue
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String parseStringParamAndSanitize(String inParam, String defaultValue) throws UnsupportedEncodingException {
        if (inParam == null) {
            return defaultValue;
        } else {
            return URLDecoder.decode(StringEscapeUtils.unescapeHtml4(POLICY.sanitize(inParam)), "UTF-8");
        }
    }

    /**
     * Parses and decodes a list of map which is contained into the given
     * inParam.
     *
     * <p>
     * In other words, transform a kind of array as: <br/>
     * [foo=bar&alice=bob, foo=baz&bobby=bobby] <br />
     * <br />
     * To a list containing the following maps:
     * <ul>
     * <li>[foo = bar, alice = bob]</li>
     * <li>[foo = baz, bobby = bobby]</li>
     * </ul>
     * </p>
     *
     * @param inParam
     * @param defaultVal
     * @param charset
     * @return
     */
    public static List<Map<String, String>> parseListMapParamAndDecode(String[] inParam, List<Map<String, String>> defaultVal, String charset) {
        if (inParam == null) {
            return defaultVal;
        }

        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (String param : inParam) {
            Map<String, String> properties = new HashMap<String, String>();
            Matcher matcher = PARAMETER_PATTERN.matcher(param);
            while (matcher.find()) {
                try {
                    properties.put(POLICY.sanitize(URLDecoder.decode(matcher.group(1), charset)), POLICY.sanitize(URLDecoder.decode(matcher.group(2), charset)));
                } catch (UnsupportedEncodingException e) {
                    return defaultVal;
                }
            }
            result.add(properties);
        }

        return result;
    }

    /**
     * Parses and decode a list from the given inParams one by decoding each of
     * them
     *
     * @param param
     * @param defaultVal
     * @param req
     * @return
     */
    public static List<String> parseListParamAndDecode(String[] inParams, List<String> defaultVal, String charset) {
        if (inParams == null) {
            return defaultVal;
        }

        List<String> result = new ArrayList<String>();
        for (String item : inParams) {
            try {
                result.add(POLICY.sanitize(URLDecoder.decode(item, charset)));
            } catch (UnsupportedEncodingException e) {
                return defaultVal;
            }
        }

        return result;
    }

    /**
     * @param inParam
     * @return an empty string if the inParam is null. It returns inParam if OK.
     */
    public static String returnEmptyStringIfNull(String inParam) {
        if (inParam != null) {
            return inParam;
        }

        return DEFAULT_SQL_STRING_VALUE;
    }

    /**
     * @param inParam
     * @return 0 if the inParam is empty or null. It returns inParam converted
     * to Integer if OK.
     * @throws NumberFormatException if inParam isn't numeric
     */
    public static int parseIntegerParam(String inParam, int defaultVal) {
        if (inParam != null && inParam.compareTo("") != 0) {
            return Integer.valueOf(inParam);
        }
        return defaultVal;
    }

    /**
     * Parses and decodes the {@link Integer} contained into the given inParam.
     *
     * @see #parseIntegerParam(String, int)
     *
     * @param inParam
     * @param defaultVal
     * @param charset
     * @return
     */
    public static int parseIntegerParamAndDecode(String inParam, int defaultVal, String charset) {
        if (inParam == null) {
            return defaultVal;
        }

        try {
            return parseIntegerParam(POLICY.sanitize(URLDecoder.decode(inParam, charset)), defaultVal);
        } catch (UnsupportedEncodingException e) {
            return defaultVal;
        } catch (NumberFormatException nfe) {
            return defaultVal;
        }
    }

    /**
     * @param inParam
     * @return 0 if the inParam is empty or null. It returns inParam converted
     * to Integer if OK.
     * @throws NumberFormatException if inParam isn't numeric
     */
    public static long parseLongParam(String inParam, long defaultVal) {
        if (inParam != null && inParam.compareTo("") != 0) {
            return Long.parseLong(inParam);
        }
        return defaultVal;
    }

    /**
     * Parses and decodes the {@link Long} contained into the given inParam.
     *
     * @see #parseLongParam(String, int)
     *
     * @param inParam
     * @param defaultVal
     * @param charset
     * @return
     */
    public static long parseLongParamAndDecode(String inParam, long defaultVal, String charset) {
        if (inParam == null) {
            return defaultVal;
        }

        try {
            return parseLongParam(POLICY.sanitize(URLDecoder.decode(inParam, charset)), defaultVal);
        } catch (UnsupportedEncodingException e) {
            return defaultVal;
        } catch (NumberFormatException nfe) {
            return defaultVal;
        }
    }

    /**
     * @param inParam
     * @param defaultVal
     * @return true if "yes", "true" or "Y", false if "no", "false" or "N" and
     * defaultVal if any over value
     */
    public static boolean parseBooleanParam(String inParam, boolean defaultVal) {
        if (inParam == null) {
            return defaultVal;
        }
        if ((inParam.equalsIgnoreCase(DEFAULT_BOOLEAN_TRUE_VALUE) || inParam.equalsIgnoreCase("yes") || inParam.equalsIgnoreCase("true"))) {
            return true;
        }
        if ((inParam.equalsIgnoreCase(DEFAULT_BOOLEAN_FALSE_VALUE) || inParam.equalsIgnoreCase("no") || inParam.equalsIgnoreCase("false"))) {
            return false;
        }
        return defaultVal;
    }

    /**
     * Parses and decodes the {@link Boolean} contained into the given inParam.
     *
     * @see #parseBooleanParam(String, int)
     *
     * @param inParam
     * @param defaultVal
     * @param charset
     * @return
     */
    public static boolean parseBooleanParamAndDecode(String inParam, boolean defaultVal, String charset) {
        if (inParam == null) {
            return defaultVal;
        }

        try {
            return parseBooleanParam(POLICY.sanitize(URLDecoder.decode(inParam, charset)), defaultVal);
        } catch (UnsupportedEncodingException e) {
            return defaultVal;
        }
    }

    /**
     * @param value
     * @param property
     * @return true if "yes", "true" or "Y", false if "no", "false" or "N" and
     * defaultVal if any over value
     */
    public static String securePassword(String value, String property) {
        if (property == null) {
            return value;
        }
        if (property.contains("PASS")) {
            return "XXXXXXXXXX";
        } else {
            return value;
        }
    }

}
