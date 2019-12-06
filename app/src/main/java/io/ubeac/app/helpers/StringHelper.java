package io.ubeac.app.helpers;

public class StringHelper {
    public static String getSnakeCaseName(String name) {
        return name.substring(name.lastIndexOf(".") + 1);
    }

    public static String getTitleCaseName(String name) {
        name = name.substring(name.lastIndexOf(".") + 1);
        StringBuilder sb = new StringBuilder(name);
        try {
            sb.replace(0, 1, String.valueOf(Character.toUpperCase(sb.charAt(0))));
            for (int i = 0; i < sb.length(); i++)
                if (sb.charAt(i) == '_')
                    sb.replace(i, i + 1, " ").replace(i + 1, i + 2, String.valueOf(Character.toUpperCase(sb.charAt(i + 1))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
