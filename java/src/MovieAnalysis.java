import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class MovieAnalysis {
    private DataDao dao;
    public MovieAnalysis(String fileName) {
        dao = new DataDao(fileName);
    }
    // 2016 年收視率最高的前三部電影
    public List<Map<String, String>> top3Movies2016() {
    return dao.getDataList().stream()
        .filter(movie -> movie.get("Year").equals("2016"))
        .sorted(Comparator.comparing(movie -> Double.parseDouble(movie.get("Rating")), Comparator.reverseOrder()))
        .limit(3)
        .collect(Collectors.toList());
    }
    // 平均收入最高的演員
    public String highestAvgRevenueActor() {
        Map<String, List<Double>> actorRevenueMap = new HashMap<>();
        
        for (Map<String, String> movie : dao.getDataList()) {
            String[] actors = movie.get("Actors").split("\\|");
            if (!movie.get("Revenue (Millions)").isEmpty()) {
                double revenue = Double.parseDouble(movie.get("Revenue (Millions)"));
                for (String actor : actors) {
                    actorRevenueMap.computeIfAbsent(actor, k -> new ArrayList<>()).add(revenue);
                }
            }
        }
    
        return actorRevenueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0)))
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
    // 與演員合作最多的前三名導演  
    public List<String> top3DirectorsForActor(String actor) {
        Map<String, Long> directorCountMap = dao.getDataList().stream()
            .filter(movie -> movie.get("Actors").contains(actor))
            .collect(Collectors.groupingBy(movie -> movie.get("Director"), Collectors.counting()));
    
        return directorCountMap.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
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

    public static List<String> fieldNameGetter(List<String> lineList) {
        return lineParser(lineList.get(0));
    }

    public static List<Map<String, String>> dataGetter(List<String> lineList, List<String> fieldNameList) {
        int lineNum = 1;
        List<Map<String, String>> dataList = new ArrayList<>();
        while(lineNum < lineList.size()) { // list size n / index 0-n-1 / lineNum 1-n-1
            Map<String, String> lineDataMap = new HashMap<>();
            List<String> lineElements = lineParser(lineList.get(lineNum));
            for(int i=0; i<fieldNameList.size(); i++) {
                lineDataMap.put(fieldNameList.get(i), lineElements.get(i)); 
            }
            dataList.add(lineDataMap);
            lineNum ++;
        }
        return dataList;
    }

    private static List<String> lineParser(String line) {
        return Arrays.asList(line.trim().split(","));
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