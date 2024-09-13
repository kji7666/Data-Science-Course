import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.LinkedHashMap;

public class Polynomial {
    private static int state;
    private final static int WITHOUT_SYMBOLS = 0;
    private final static int WITH_SYMBOLS = 1;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Input the polynomials: ");
        String input = scanner.nextLine(); 
        String result = Polynomial.multiplyPolynomials(input);
        System.out.println("Output Result : " + result);
        System.out.println("Bonus Output Result : " + Polynomial.removeSymbols(result));
    }

    public static String multiplyPolynomials(String polynomialString) {
        determineState(polynomialString); // 確定輸出模式
        polynomialString = removeSymbols(polynomialString); // 格式化字串方便處理
        Map<String, Integer> resultMap = parsePolynomial(polynomialString);
        String result = formatOutput(resultMap);
        return result;
    }

    private static void determineState(String polynomialString) {
        if (polynomialString.contains("*") || polynomialString.contains("^")) {
            state = WITH_SYMBOLS;
        } else {
            state = WITHOUT_SYMBOLS;
        }
    }

    private static String removeSymbols(String polynomialString) {
        return polynomialString.replaceAll("\\*", "")
                                .replaceAll("\\^", "");
    }

    private static Map<String, Integer> parsePolynomial(String polynomialString) {
        //System.out.println("[" + polynomialString.replaceAll("\\s+", "").replaceAll("\\)", "").substring(1) + "]");
        String[] polynomialArray;
        if (polynomialString.contains("(")) {  // 有多個多項式相乘
            polynomialArray = polynomialString.replaceAll("\\s+", "")
                                    .replaceAll("\\)", "")
                                    .substring(1)
                                    .split("\\(");
        } else {  // 單一多項式
            polynomialArray = new String[]{polynomialString};
        }
        ArrayList<LinkedHashMap<String, Integer>> variableList = new ArrayList<>(); // 多項式 -> 變數 -> 參數
        int count = 0;
        for (String polynomial : polynomialArray) {
            //System.out.println(polynomial + "/");
            LinkedHashMap<String, Integer> variableMap = new LinkedHashMap<>();
            // 替換多項式中的 + 和 -，方便後續分割
            String[] terms = polynomial.replaceAll("\\+", " ")
                                        .replaceAll("\\-", " -")
                                        .split("\\s+");
            for (String term : terms) {
                if (term.isEmpty()) continue;  
                int coefficient = 1; // 默認係數為 1
                boolean isNegative = false;
                int variableIndex = 0;
                // 檢查是否為負號
                if (term.startsWith("-")) {
                    isNegative = true;
                    term = term.substring(1);
                } 
                // 找到變數開始的位置
                for (int i = 0; i < term.length(); i++) {
                    char ch = term.charAt(i);
                    if (Character.isLetter(ch)) {
                        variableIndex = i;
                        break;
                    }
                }
                // get varible / coefficient
                if (variableIndex >= 0) {
                    try {
                        coefficient = Integer.parseInt(term.substring(0, variableIndex));
                    } catch (NumberFormatException e) {
                        coefficient = 1; // 如果係數部分為空，則默認為 1
                    }
                    if (isNegative) {
                        coefficient *= -1; // 根據符號調整係數
                    }
                    String variable = term.substring(variableIndex);
                    variableMap.put(variable, coefficient);
                } else {
                    // 如果沒有變數，假設整個 term 是常數
                    try {
                        coefficient = Integer.parseInt(term);
                        if (isNegative) {
                            coefficient *= -1;
                        }
                        variableMap.put("", coefficient); // 空字串表示沒有變數
                    } catch (NumberFormatException e) {
                        // 處理異常情況
                        System.err.println("Invalid term format: " + term);
                    }
                }
            }
            variableList.add(variableMap);
        }
        return multiplication(variableList);
    }

    private static Map<String, Integer> multiplication(ArrayList<LinkedHashMap<String, Integer>> variableList) {
        while(variableList.size() > 1) {
            LinkedHashMap<String, Integer> resultMap = new LinkedHashMap<>();
            LinkedHashMap<String, Integer> firstPolynomial = variableList.get(0);
            LinkedHashMap<String, Integer> secondPolynomial = variableList.get(1);
            for(Map.Entry<String, Integer> firstEntry : firstPolynomial.entrySet()) {
                for(Map.Entry<String, Integer> secondEntry : secondPolynomial.entrySet()) {
                    String newVariable = multiplyVariables(firstEntry.getKey(), secondEntry.getKey());
                    int newCoefficient = firstEntry.getValue() * secondEntry.getValue();
                    resultMap.put(newVariable, resultMap.getOrDefault(newVariable, 0) + newCoefficient);
                }
            }
            variableList.remove(0);
            variableList.remove(0);
            variableList.add(resultMap);
        }
        return variableList.get(0);
    }
        

    private static String multiplyVariables(String var1, String var2) {
        return expandPolynomial(var1 + var2);
    }

    private static String expandPolynomial(String polynomial) {
        StringBuilder result = new StringBuilder();
        int length = polynomial.length();
        int i = 0;
        while (i < length) {
            char ch = polynomial.charAt(i);
            // 檢查是否是字母
            if (Character.isLetter(ch)) {
                char variable = ch;
                i++;
                // 抓字母後數字(一個一個拼)
                StringBuilder number = new StringBuilder();
                while (i < length && Character.isDigit(polynomial.charAt(i))) {
                    number.append(polynomial.charAt(i));
                    i++;
                }
                // 沒有數字默認為 1
                int count = number.length() > 0 ? Integer.parseInt(number.toString()) : 1;
                
                // 展開變數(X2Y3 => XXYYY)
                for (int j = 0; j < count; j++) {
                    result.append(variable);
                }
            } else {
                i++;
            }
        }
        
        return result.toString();
    }

    private static String formatOutput(Map<String, Integer> resultMap) {
        Map<String, Integer> recordMap = new LinkedHashMap<>();
        for(Map.Entry<String, Integer> resultEntry : resultMap.entrySet()) {
            Map<String, Integer> powerMap = new HashMap<>(); // 變數 -> 次方
            String variable = resultEntry.getKey();
            for(int i=0; i<variable.length(); i++) {
                String ch = String.valueOf(variable.charAt(i));
                powerMap.put(ch, powerMap.getOrDefault(ch, 0) + 1);
            }
            StringBuilder formatSb = new StringBuilder();
            for(Map.Entry<String, Integer> powerEntry : powerMap.entrySet()) {
                formatSb.append(powerEntry.getKey() + (powerEntry.getValue() != 1 ? "^" + powerEntry.getValue() : ""));
            }
            String formatVariable = formatSb.toString();
            if(recordMap.containsKey(formatVariable)) {
                recordMap.put(formatVariable, recordMap.get(formatVariable) + resultEntry.getValue());
            } else {
                recordMap.put(formatVariable, resultEntry.getValue());
            }
        } // 和併


        StringBuilder sb = new StringBuilder("");
        for(Map.Entry<String, Integer> entry : recordMap.entrySet()) {
            if(entry.getValue() < -1) {
                sb.append(entry.getValue() + "*");
            } else if(entry.getValue() == -1) {
                sb.append("-");
            } else if(entry.getValue() == 1) {
                sb.append("+");
            } else {
                sb.append("+" + entry.getValue() + "*");
            }
            sb.append(entry.getKey());
        }

        return sb.toString().substring(1); // 去掉開頭的"+"
    }
}
