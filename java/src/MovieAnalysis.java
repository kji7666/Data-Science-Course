import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class MovieAnalysis {
    public static void main(String[] args) {
        MovieAnalysis movieAnalysis = new MovieAnalysis("src/IMDB-Movie-Data.csv");
        System.out.println("第一題 : 2016 年收視率最高的前三部電影的標題");
        int count = 1;
        for(String movie : movieAnalysis.top3Movies2016()) {
            System.out.println(count + ". " + movie);
            count ++;
        }
        System.out.println("第二題 : 平均收入最高的演員");
        System.out.println(movieAnalysis.highestAvgRevenueActor());
        System.out.println("第三題 : 艾瑪華森電影的平均分數是多少？");
        System.out.println(movieAnalysis.avgRatingForEmmaWatson());
        System.out.println("第四題 : 與演員合作最多的前三名導演");
        count = 1;
        for(String movie : movieAnalysis.top3DirectorsWithMostCollaborations()) {
            System.out.println(count + ". " + movie);
            count ++;
        }
        System.out.println("第五題 : 出演最多類型電影的前兩名演員");
        count = 1;
        for(String movie : movieAnalysis.top2ActorsWithMostGenres()) {
            System.out.println(count + ". " + movie);
            count ++;
        }
        System.out.println("第六題 : 電影中年數差距最大的前三名演員");
        count = 1;
        for(String movie : movieAnalysis.top3ActorsWithMaxYearGap()) {
            System.out.println(count + ". " + movie);
            count ++;
        }
        System.out.println("第七題 : 尋找所有與強尼戴普直接和間接合作的演員");
        count = 1;
        for(String movie : movieAnalysis.actorsCooperatedWithJohnnyDepp()) {
            System.out.println(count + ". " + movie);
            count ++;
        }
    }

    private DataDao dao;
    public MovieAnalysis(String fileName) {
        dao = new DataDao(fileName);
    }
    // 2016 年收視率最高的前三部電影的標題
    public List<String> top3Movies2016() {
        return dao.getDataList().stream()
            .filter(movie -> movie.get("Year").equals("2016")) // 過濾出2016年的電影
            .sorted(Comparator.comparing(movie -> Double.parseDouble(movie.get("Rating")), Comparator.reverseOrder())) // 按收視率排序
            .limit(3) // 取前三部
            .map(movie -> movie.get("Title")) // 取標題
            .collect(Collectors.toList()); // 收集成列表
    }
    // 平均收入最高的演員
    public String highestAvgRevenueActor() {
        // Map 每位演員到他們的收入列表
        Map<String, List<Double>> actorRevenueMap = new HashMap<>();

        for (Map<String, String> movie : dao.getDataList()) {
            String[] actors = movie.get("Actors").split("\\|");
            // 去除每個名字的前後空白
            actors = Arrays.stream(actors).map(String::trim).toArray(String[]::new);
            
            String revenueStr = movie.get("Revenue (Millions)");
            if (revenueStr != null && !revenueStr.isEmpty()) {
                try {
                    double revenue = Double.parseDouble(revenueStr);
                    for (String actor : actors) {
                        actorRevenueMap.computeIfAbsent(actor, k -> new ArrayList<>()).add(revenue);
                    }
                } catch (NumberFormatException e) {
                    // 如果收入數據無法解析，忽略此條記錄
                    System.err.println("Invalid revenue data: " + revenueStr);
                }
            }
        }

        // 計算每位演員的平均收入並返回收入最高的演員
        return actorRevenueMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0)
            ))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("No data available");
    }
    // 艾瑪華森電影的平均分數是多少？
    public double avgRatingForEmmaWatson() {
        return dao.getDataList().stream()
            .filter(movie -> movie.get("Actors").contains("Emma Watson"))
            .mapToDouble(movie -> Double.parseDouble(movie.get("Rating")))
            .average()
            .orElse(0);
    }
    // 與最多不同演員合作的前三名導演
    public List<String> top3DirectorsWithMostCollaborations() {
        // 收集每位導演與不同演員的合作數量
        Map<String, Set<String>> directorActorsMap = dao.getDataList().stream()
            .collect(Collectors.groupingBy(
                movie -> movie.get("Director"),
                Collectors.mapping(movie -> movie.get("Actors").split("\\|"),
                    Collectors.flatMapping(Arrays::stream, Collectors.toSet())
                )
            ));

        // 計算每位導演合作的不同演員數量並排序
        return directorActorsMap.entrySet().stream()
            .sorted(Map.Entry.<String, Set<String>>comparingByValue((s1, s2) -> Integer.compare(s2.size(), s1.size())))
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    // 出演最多類型電影的前兩名演員
    public List<String> top2ActorsWithMostGenres() {
        Map<String, Set<String>> actorGenresMap = new HashMap<>();
        
        for (Map<String, String> movie : dao.getDataList()) {
            String[] actors = movie.get("Actors").split("\\|");
            String[] genres = movie.get("Genre").split("\\|");
            for (String actor : actors) {
                actorGenresMap.computeIfAbsent(actor, k -> new HashSet<>()).addAll(Arrays.asList(genres));
            }
        }
    
        return actorGenresMap.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .limit(2)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    // 電影中年數差距最大的前三名演員
    public List<String> top3ActorsWithMaxYearGap() {
        Map<String, List<Integer>> actorYearMap = new HashMap<>();
        
        for (Map<String, String> movie : dao.getDataList()) {
            String[] actors = movie.get("Actors").split("\\|");
            int year = Integer.parseInt(movie.get("Year"));
            for (String actor : actors) {
                actorYearMap.computeIfAbsent(actor, k -> new ArrayList<>()).add(year);
            }
        }
    
        return actorYearMap.entrySet().stream()
            .map(entry -> {
                List<Integer> years = entry.getValue();
                int maxYearGap = Collections.max(years) - Collections.min(years);
                return new AbstractMap.SimpleEntry<>(entry.getKey(), maxYearGap);
            })
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    // 尋找所有與強尼戴普直接和間接合作的演員
    public Set<String> actorsCooperatedWithJohnnyDepp() {
        Set<String> cooperatedActors = new HashSet<>();
        Stack<String> actorStack = new Stack<>();
        actorStack.push("Johnny Depp");
        cooperatedActors.add("Johnny Depp");
    
        while (!actorStack.isEmpty()) {
            String currentActor = actorStack.pop();
            for (Map<String, String> movie : dao.getDataList()) {
                if (movie.get("Actors").contains(currentActor)) {
                    String[] actors = movie.get("Actors").split("\\|");
                    for (String actor : actors) {
                        if (!cooperatedActors.contains(actor)) {
                            cooperatedActors.add(actor);
                            actorStack.push(actor);
                        }
                    }
                }
            }
        }
    
        return cooperatedActors;
    }
                  
}

class FileParser {
    // 讀取文件的每一行
    public static List<String> fileReader(String fileName) {
        List<String> lineList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lineList;
    }

    // 獲取欄位名（首行）
    public static List<String> fieldNameGetter(List<String> lineList) {
        return lineParser(lineList.get(0));
    }

    // 獲取數據列表，處理缺失數據
    public static List<Map<String, String>> dataGetter(List<String> lineList, List<String> fieldNameList) {
        int lineNum = 1; // 從第二行開始（第一行是欄位名）
        List<Map<String, String>> dataList = new ArrayList<>();
        
        while (lineNum < lineList.size()) { // list size n / index 0-n-1 / lineNum 1-n-1
            List<String> lineElements = lineParser(lineList.get(lineNum));
            Map<String, String> lineDataMap = new HashMap<>();
            
            // 確保每個欄位都有數據，如果缺少數據則設置為 "0"
            for (int i = 0; i < fieldNameList.size(); i++) {
                String value = (i < lineElements.size()) ? lineElements.get(i) : "0"; // 使用 "0" 作為預設值
                lineDataMap.put(fieldNameList.get(i), value.isEmpty() ? "0" : value); // 空值設置為 "0"
            }
            
            dataList.add(lineDataMap);
            lineNum++;
        }
        return dataList;
    }

    // 將行分割成字段
    private static List<String> lineParser(String line) {
        String[] elements = line.split(",", -1); // 使用 -1 參數以保留所有空字符串
        return Arrays.asList(elements);
    }
}

class DataDao {
    private List<String> lineList = new ArrayList<>(); // 文件各行
    private List<Map<String, String>> dataList = new ArrayList<>(); // 不包含欄位名
    private List<String> fieldNames = new ArrayList<>();

    public DataDao(String fileName) {
        lineList = FileParser.fileReader(fileName);
        fieldNames = FileParser.fieldNameGetter(lineList);
        dataList = FileParser.dataGetter(lineList, fieldNames);
    }

    public List<String> getLineList() {
        return lineList;
    }

    public List<Map<String, String>> getDataList() {
        return dataList;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void sortByValue(String fieldName, boolean isReverseOrder) {
        if(fieldNames.contains(fieldName)) {
            try{
                if(isReverseOrder) {
                        dataList.sort(Comparator.comparing(dataLineMap -> Double.parseDouble(dataLineMap.get(fieldName)), Comparator.reverseOrder()));
                } else {
                    dataList.sort(Comparator.comparing(dataLineMap -> Double.parseDouble(dataLineMap.get(fieldName))));
                }
            } catch (Exception e) {
                System.out.println("此列非數值, 無法排序");
            }
        }
    }
    
}