import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

public class MovieAnalysis {
    public static void main(String[] args) {
        String outputPath = "java\\src\\output.txt";
        MovieAnalysis movieAnalysis = new MovieAnalysis("java\\src\\IMDB-Movie-Data.csv");
        System.out.println("第一題 : 2016 年收視率最高的前三部電影的標題");
        FileParser.fileAppend("第一題 : 2016 年收視率最高的前三部電影的標題", outputPath);
        int count = 1;
        for(String movie : movieAnalysis.top3Movies2016()) {
            System.out.println(count + ". " + movie);
            FileParser.fileAppend(count + ". " + movie, outputPath);
            count ++;
        }
        FileParser.fileAppend("", outputPath);

        System.out.println("第二題 : 平均收入最高的演員");
        FileParser.fileAppend("第二題 : 平均收入最高的演員", outputPath);
        System.out.println(movieAnalysis.highestAvgRevenueActor());
        //FileParser.fileAppend(movieAnalysis.highestAvgRevenueActor(), outputPath);
        FileParser.fileAppend("", outputPath);

        System.out.println("第三題 : 艾瑪華森電影的平均分數是多少？");
        FileParser.fileAppend("第三題 : 艾瑪華森電影的平均分數是多少？", outputPath);
        System.out.println(movieAnalysis.avgRatingForEmmaWatson());
        FileParser.fileAppend(String.valueOf(movieAnalysis.avgRatingForEmmaWatson()), outputPath);
        FileParser.fileAppend("", outputPath);

        System.out.println("第四題 : 與演員合作最多的前三名導演");
        FileParser.fileAppend("第四題 : 與演員合作最多的前三名導演", outputPath);
        count = 1;
        for(String movie : movieAnalysis.top3DirectorsWithMostCollaborations()) {
            System.out.println(count + ". " + movie);
            FileParser.fileAppend(count + ". " + movie, outputPath);
            count ++;
        }
        FileParser.fileAppend("", outputPath);

        System.out.println("第五題 : 出演最多類型電影的前兩名演員");
        FileParser.fileAppend("第五題 : 出演最多類型電影的前兩名演員", outputPath);
        count = 1;
        for(String movie : movieAnalysis.top2ActorsWithMostGenres()) {
            System.out.println(count + ". " + movie);
            FileParser.fileAppend(count + ". " + movie, outputPath);
            count ++;
        }
        FileParser.fileAppend("", outputPath);

        System.out.println("第六題 : 電影中年數差距最大的前三名演員");
        FileParser.fileAppend("第六題 : 電影中年數差距最大的前三名演員", outputPath);
        count = 1;
        for(String movie : movieAnalysis.top3ActorsWithMaxYearGap()) {
            System.out.println(count + ". " + movie);
            FileParser.fileAppend(count + ". " + movie, outputPath);
            count ++;
        }
        FileParser.fileAppend("", outputPath);

        System.out.println("第七題 : 尋找所有與強尼戴普直接和間接合作的演員");
        FileParser.fileAppend("第七題 : 尋找所有與強尼戴普直接和間接合作的演員", outputPath);
        count = 1;
        for(String movie : movieAnalysis.actorsCooperatedWithJohnnyDepp()) {
            System.out.println(count + ". " + movie);
            FileParser.fileAppend(count + ". " + movie, outputPath);
            count ++;
        }
    }

    private DataDao dao;
    public MovieAnalysis(String fileName) {
        dao = new DataDao(fileName);
    }
    public List<String> top3Movies2016() {
        // 過濾出2016年的電影並按收視率降序排序
        List<Map<String, String>> sortedMovies2016 = dao.getDataList().stream()
            .filter(movie -> movie.get("Year").equals("2016")) // 過濾2016年的電影
            .sorted(Comparator.comparing(movie -> Double.parseDouble(movie.get("Rating")), Comparator.reverseOrder())) // 按收視率降序排序
            .collect(Collectors.toList());
    
        // 初始化結果列表和收視率計數
        List<String> topMovies = new ArrayList<>();
        int count = 0;
        double minRatingOfTop3 = -1; // 用於存儲第三名的收視率
    
        // 遍歷排序後的電影，選擇前三名及相同收視率的電影
        for (Map<String, String> movie : sortedMovies2016) {
            double rating = Double.parseDouble(movie.get("Rating"));
            if (count < 3 || rating == minRatingOfTop3) {
                topMovies.add(movie.get("Title"));
                if (count < 3) {
                    count++;
                    if (count == 3) {
                        minRatingOfTop3 = rating; // 設置第三名的收視率
                    }
                }
                System.out.println("rating = " + rating);
            } else {
                break; // 當出現比第三名還低的收視率時，停止
            }
        }
    
        return topMovies;
    }
    
    public List<String> highestAvgRevenueActor() {
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
    
        // 計算每位演員的平均收入
        Map<String, Double> actorAvgRevenueMap = actorRevenueMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0)
            ));
    
        // 找出最高的平均收入
        double highestAvgRevenue = actorAvgRevenueMap.values().stream()
            .max(Double::compare)
            .orElse(0.0);
    
        // 找出所有擁有最高平均收入的演員
        List<String> highestAvgRevenueActors = actorAvgRevenueMap.entrySet().stream()
            .filter(entry -> entry.getValue() == highestAvgRevenue)
            .map(entry -> {
                String actor = entry.getKey();
                List<Double> revenues = actorRevenueMap.get(actor);
                int movieCount = revenues.size();  // 取得演員的電影數量
                double totalRevenue = revenues.stream().mapToDouble(Double::doubleValue).sum();  // 計算總收入
                // 打印場次和總收入
                System.out.println("收入最高的演員: " + actor + " (場次: " + movieCount + " / 總收入: " + totalRevenue + ")");
                return actor;
            })
            .collect(Collectors.toList());
    
        return highestAvgRevenueActors;
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
            Collectors.flatMapping(
                movie -> Arrays.stream(movie.get("Actors").split("\\|")),
                Collectors.toSet()
            )
        ));

    // 初始化結果列表和合作數計數
    List<Map.Entry<String, Set<String>>> sortedDirectors = directorActorsMap.entrySet().stream()
        .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size())) // 按合作數量降序排序
        .collect(Collectors.toList());

    List<String> topDirectors = new ArrayList<>();
    int count = 0;
    int minCollaborationsOfTop3 = -1;  // 用於存儲第三名的合作數量

    // 遍歷排序後的導演，選擇前三名及相同合作數量的導演
    for (Map.Entry<String, Set<String>> entry : sortedDirectors) {
        int collaborations = entry.getValue().size();
        if (count < 3 || collaborations == minCollaborationsOfTop3) {
            topDirectors.add(entry.getKey());
            if (count < 3) {
                count++;
                if (count == 3) {
                    minCollaborationsOfTop3 = collaborations; // 設置第三名的合作數量
                }
            }
            // 打印導演及其合作演員
            System.out.println(entry.getKey() + " : " + collaborations + " 合作演員: " + entry.getValue());
        } else {
            break; // 當出現比第三名還少的合作數時，停止
        }
    }

    return topDirectors;
}

    
    public List<String> top2ActorsWithMostGenres() {
        Map<String, Set<String>> actorGenresMap = new HashMap<>();
        
        // 收集每位演員出演的不同類型電影
        for (Map<String, String> movie : dao.getDataList()) {
            String[] actors = movie.get("Actors").split("\\|");
            String[] genres = movie.get("Genre").split("\\|");
            for (String actor : actors) {
                actorGenresMap.computeIfAbsent(actor, k -> new HashSet<>()).addAll(Arrays.asList(genres));
            }
        }
    
        // 將演員根據出演類型數量降序排序
        List<Map.Entry<String, Set<String>>> sortedActors = actorGenresMap.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .collect(Collectors.toList());
    
        List<String> topActors = new ArrayList<>();
        int count = 0;
        int minGenresOfTop2 = -1;  // 用於存儲第二名的類型數量
    
        // 遍歷排序後的演員，選擇前兩名及相同類型數量的演員
        for (Map.Entry<String, Set<String>> entry : sortedActors) {
            int genresCount = entry.getValue().size();
            if (count < 2 || genresCount == minGenresOfTop2) {
                topActors.add(entry.getKey());
                if (count < 2) {
                    count++;
                    if (count == 2) {
                        minGenresOfTop2 = genresCount; // 設置第二名的類型數量
                    }
                }
                System.out.println(entry.getKey() + ":" + genresCount);
            } else {
                break; // 當出現比第二名還少的類型數量時，停止
            }
        }
    
        return topActors;
    }
    
    public List<String> top3ActorsWithMaxYearGap() {
        Map<String, List<Integer>> actorYearMap = new HashMap<>();
    
        // 收集每位演員的年份資料
        for (Map<String, String> movie : dao.getDataList()) {
            String[] actors = movie.get("Actors").split("\\|");
            int year = Integer.parseInt(movie.get("Year"));
            for (String actor : actors) {
                actorYearMap.computeIfAbsent(actor, k -> new ArrayList<>()).add(year);
            }
        }
    
        // 計算每位演員的年份差距並排序
        List<Map.Entry<String, Integer>> actorYearGapList = actorYearMap.entrySet().stream()
            .map(entry -> {
                List<Integer> years = entry.getValue();
                int maxYearGap = Collections.max(years) - Collections.min(years);
                return new AbstractMap.SimpleEntry<>(entry.getKey(), maxYearGap);
            })
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .collect(Collectors.toList());
    
        // 獲取前三名最大差距，並找出與第三名差距相同的演員
        List<String> topActors = new ArrayList<>();
        int count = 0;
        int minGapOfTop3 = -1;  // 用於儲存第三名的差距
    
        for (Map.Entry<String, Integer> entry : actorYearGapList) {
            int gap = entry.getValue();
            if (count < 3 || gap == minGapOfTop3) {
                topActors.add(entry.getKey());
                if (count < 3) {
                    count++;
                    if (count == 3) {
                        minGapOfTop3 = gap;  // 設定第三名的差距
                    }
                }
                System.out.println(entry.getKey() + ":" + gap);
            } else {
                break;  // 當出現比第三名還小的差距時，停止
            }
        }
    
        return topActors;
    }
    
    public Set<String> actorsCooperatedWithJohnnyDepp() {
        Set<String> cooperatedActors = new HashSet<>();  // 儲存已合作的演員
        Stack<String> actorStack = new Stack<>();  // 用於遍歷演員
        actorStack.push("Johnny Depp");  // 開始於強尼戴普
        cooperatedActors.add("Johnny Depp");  // 將強尼戴普加入已合作名單
    
        // 深度優先搜尋演員
        while (!actorStack.isEmpty()) {
            String currentActor = actorStack.pop();  // 取得目前演員
            for (Map<String, String> movie : dao.getDataList()) {  // 獲取所有電影資料
                if (movie.get("Actors").contains(currentActor)) {  // 檢查目前演員是否參與該電影
                    String[] actors = movie.get("Actors").split("\\|");  // 獲取所有演員
                    for (String actor : actors) {
                        if (!cooperatedActors.contains(actor)) {  // 如果演員尚未被加入
                            cooperatedActors.add(actor);  // 將演員加入已合作名單
                            actorStack.push(actor);  // 將演員推入棧中以進行後續檢查
                        }
                    }
                }
            }
        }
    
        return cooperatedActors;  // 返回所有合作的演員
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

    public static void fileAppend(String line, String fileName) {
        BufferedWriter writer = null;
        try {
            // 開啟 FileWriter 並設定為追加模式
            writer = new BufferedWriter(new FileWriter(fileName, true));
            // 將字串寫入檔案
            writer.write(line);
            writer.newLine();  // 可選，新增換行符號
        } catch (IOException e) {
            e.printStackTrace();  // 處理可能的 IO 例外
        } finally {
            try {
                if (writer != null) {
                    writer.close();  // 關閉 BufferedWriter
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

