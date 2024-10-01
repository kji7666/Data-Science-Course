DATA_INPUT_PATH = "python/IMDB-Movie-Data.csv"

# 如果你需要output到txt, 請在最下方啟用file append
output_path = "python\\output.txt"

class FileParser:
    '''
    string and file parse
    '''
    # 讀取文件的每一行
    @staticmethod # static => @staticmethod
    def file_reader(file_name):
        line_list = []
        try:
            with open(file_name, 'r', encoding='utf-8') as file: # 一行一行截
                for line in file:
                    line_list.append(line.strip())  # 去除行末尾的換行符號
        except IOError as e:
            print(f"文件讀取錯誤: {e}")
        return line_list

    # 獲取欄位名（首行）
    @staticmethod
    def field_name_getter(line_list):
        if line_list:
            return FileParser.line_parser(line_list[0])  # 解析首行作為欄位名
        return [] # list

    # 獲取數據列表，處理缺失數據
    @staticmethod
    def data_getter(line_list, field_name_list):
        data_list = []
        DEFALUE_VALUE = "0"
        for line_num in range(1, len(line_list)):  # 從第二行開始處理
            line_elements = FileParser.line_parser(line_list[line_num])  # 分割資料
            line_data_map = {}

            # 確保每個欄位都有數據，如果缺少數據(",,"造成"")則設置為 "0"
            for i in range(len(field_name_list)):
                value = line_elements[i] if i < len(line_elements) else DEFALUE_VALUE  # 超出field處理
                line_data_map[field_name_list[i]] = value if value else DEFALUE_VALUE  # 空值設置為 "0"
            data_list.append(line_data_map)

        return data_list

    # 解析每一行，將字符串按","分割為字段
    @staticmethod
    def line_parser(line):
        return line.split(',')  # 使用逗號分割字符串

    # 將line string附加到文件中
    @staticmethod
    def file_append(line, file_name):
        try:
            with open(file_name, 'a', encoding='utf-8') as file:
                file.write(line + '\n') 
        except IOError as e:
            print(f"文件寫入錯誤: {e}")

class DataDao:
    '''
    data store : every line == a map , field mapping value (all string)
    '''
    def __init__(self, file_name):
        self.line_list = FileParser.file_reader(file_name)  # 讀整個文件 切成list # lines
        self.field_names = FileParser.field_name_getter(self.line_list)  # 取欄位名 # field
        self.data_list = FileParser.data_getter(self.line_list, self.field_names)  # 整理數據 # line list => field mapping data

    # getter
    def get_line_list(self):
        return self.line_list

    def get_data_list(self):
        return self.data_list

    def get_field_names(self):
        return self.field_names

    # 根據指定field進行sort(限定數值類型)
    def sort_by_value(self, field_name, is_reverse_order):
        if field_name in self.field_names:
            try:
                # string to double進行排序
                self.data_list.sort(key=lambda data_line_map: float(data_line_map.get(field_name, 0)), 
                                    reverse=is_reverse_order)
            except ValueError:
                print(f"欄位 '{field_name}' 不是數字類型，無法排序")

class MovieAnalysis:
    '''
    anwser
    '''
    # attribute : dao
    def __init__(self, file_name):
        self.dao = DataDao(file_name)

    # 第一題
    def top3_movies_2016(self):
        # 從資料中篩選出2016年的電影並按收視率由高到低排序
        sorted_movies_2016 = sorted( # for-each 遍歷 movie(every line data) 過濾 : year == 2016
            (movie for movie in self.dao.get_data_list() if movie["Year"] == "2016"),
            key=lambda movie: float(movie["Rating"]), # key : sort 依據 # movie rating (string to double)
            reverse=True # 降冪
        )

        top_movies = []  # 存放前3名的電影
        min_rating_of_top3 = None  # 記錄第3名的最低評分

        for movie in sorted_movies_2016:
            rating = float(movie["Rating"])

            # 如果還沒選滿3部電影 or 當前電影評分等於第3名的rating, 加入list
            if len(top_movies) < 3 or rating == min_rating_of_top3:
                top_movies.append(movie["Title"])
                # 如果加入的是第3部電影, 記錄第3名的rating
                if len(top_movies) == 3:
                    min_rating_of_top3 = rating
            else:
                break

        return top_movies

    # 第二題
    def highest_avg_revenue_actor(self):
        actor_revenue_map = {} # actor mapping revenue
        # 遍歷所有電影資料
        for movie in self.dao.get_data_list():
            actors = [actor.strip() for actor in movie["Actors"].split("|")] # data_list get actors string => split by "|" => strip every actors
            revenue_str = movie.get("Revenue (Millions)")
            if revenue_str:
                try:
                    revenue = float(revenue_str)
                    for actor in actors:
                        # actor => revenue list
                        actor_revenue_map.setdefault(actor, []).append(revenue)
                except ValueError:
                    print(f"Invalid revenue data: {revenue_str}")
        # actor => avg revenue
        actor_avg_revenue_map = {
            actor: sum(revenues) / len(revenues) for actor, revenues in actor_revenue_map.items() # items = entrySet
        }
        highest_avg_revenue = max(actor_avg_revenue_map.values(), default=0)
        # 找出其他平均收入等於最高平均收入的演員
        highest_avg_revenue_actors = [
            actor for actor, avg_revenue in actor_avg_revenue_map.items() if avg_revenue == highest_avg_revenue
        ]
        return highest_avg_revenue_actors

    # 計算艾瑪華森電影的平均分數
    def avg_rating_for_emma_watson(self):
        movies_with_emma = [movie for movie in self.dao.get_data_list() if "Emma Watson" in movie["Actors"]]
        ratings = []
        for movie in movies_with_emma:
            try:
                rating = float(movie["Rating"])
                ratings.append(rating)
            except ValueError:
                print(f"無效的評分數據: {movie['Rating']}") 
        # 計算平均評分
        if ratings:  # 有效rating
            average_rating = sum(ratings) / len(ratings)
            return average_rating
        else:
            return 0  # 如果沒有評分，則返回0

    # 與最多不同演員合作的前三名導演及所有導演的排行榜
    def top_directors_with_collaborations(self):
        director_actors_map = {}
        for movie in self.dao.get_data_list():
            director = movie["Director"]
            actors = set(actor.strip() for actor in movie["Actors"].split("|"))  # set => 演員不重複
            if director in director_actors_map:
                director_actors_map[director].update(actors)
            else:
                director_actors_map[director] = actors

        sorted_directors = sorted(
            director_actors_map.items(),
            key=lambda entry: len(entry[1]),  # 根據合作演員數量(len(entry[1]))sort
            reverse=True
        )
        top_directors = []
        count = 0 # 數top3
        min_collaborations_of_top3 = -1 # 目前最低合作人數
        for director, actors in sorted_directors:
            collaborations = len(actors)
            if count < 3 or collaborations == min_collaborations_of_top3:
                top_directors.append(director)
                if count < 3:
                    count += 1
                    if count == 3:
                        min_collaborations_of_top3 = collaborations
            else:
                break
        return top_directors, sorted_directors

    # 第五題 : 出演最多類型電影的前兩名演員
    def top2_actors_with_most_genres(self):
        actor_genres_map = {}
        # 收集演員及其演出的不同類型
        for movie in self.dao.get_data_list():
            actors = [actor.strip() for actor in movie["Actors"].split("|")]  # 此電影演員名單
            genres = set(movie["Genre"].split("|"))  # 使用 set 自動去重
            for actor in actors:
                if actor in actor_genres_map:
                    actor_genres_map[actor].update(genres) # 加入map => genres set
                else:
                    actor_genres_map[actor] = set(genres)

        # 根據不同類型數量(len(entry[1])排序
        sorted_actors = sorted(
            actor_genres_map.items(),
            key=lambda entry: len(entry[1]),
            reverse=True
        )

        top_actors = []
        count = 0 # 同上
        min_genres_of_top2 = -1

        # 找出前兩名
        for actor, genres in sorted_actors:
            genres_count = len(genres)
            if count < 2 or genres_count == min_genres_of_top2:
                top_actors.append(actor)
                if count < 2:
                    count += 1
                    if count == 2:
                        min_genres_of_top2 = genres_count
            else:
                break
        return top_actors

    # 第六題 : 演過(電影年數)差距最大的前三名演員
    def top3_actors_with_max_year_gap(self):
        actor_year_map = {}
        for movie in self.dao.get_data_list():
            actors = [actor.strip() for actor in movie["Actors"].split("|")]  # 此電影演員
            year = int(movie["Year"]) # 電影年分
            for actor in actors:
                actor_year_map.setdefault(actor, []).append(year)

        # 計算每位演員的演過(電影年數)最大差距
        actor_year_gap_list = [
            (actor, max(years) - min(years)) for actor, years in actor_year_map.items() if len(years) > 1  # 確保演員有>=2部電影
        ] # 前前 對 後後

        # 根據年份差距排序
        sorted_actor_year_gap = sorted(actor_year_gap_list, key=lambda x: x[1], reverse=True) # [1] == years

        top_actors = []
        count = 0 # 同上
        min_gap_of_top3 = -1

        # 找出前三名演員及其年份差距
        for actor, gap in sorted_actor_year_gap:
            if count < 3 or gap == min_gap_of_top3:
                top_actors.append((actor, gap))  # 儲存演員及其年份差距
                if count < 3:
                    count += 1
                    if count == 3:
                        min_gap_of_top3 = gap
            else:
                break

        # 印出結果
        # for actor, gap in top_actors:
        #     print(f"演員: {actor}, 年份差距: {gap} 年")

        return [actor for actor, gap in top_actors]

    # 第七題 : 尋找所有與強尼戴普直接和間接合作的演員
    def actors_cooperated_with_johnny_depp_2(self):
        depp_cooperated_actors = set(["Johnny Depp"])
        new_count = 1  # init : Johnny Depp
        # 暴力迴圈 直到名單人數不再增加
        while new_count > 0:
            current_count = len(depp_cooperated_actors) 
            new_count = 0 # 本次loop增加人數

            for movie in self.dao.get_data_list():
                actors = [actor.strip() for actor in movie["Actors"].split("|") if actor.strip()]
                if any(actor in depp_cooperated_actors for actor in actors):
                    for actor in actors:
                        if actor not in depp_cooperated_actors:
                            depp_cooperated_actors.add(actor)
                            new_count += 1  # 

        depp_cooperated_actors.remove("Johnny Depp") # 本人不算
        return sorted(depp_cooperated_actors)  # ascll sort

            
def main():
    movie_analysis = MovieAnalysis(DATA_INPUT_PATH)
    result = ""
    result += "第一題 : 2016 年收視率最高的前三部電影的標題" + "\n"
    count = 1
    for movie in movie_analysis.top3_movies_2016():
        result += str(count) + ". " + movie + "\n"
        count += 1

    result += "第二題 : 平均收入最高的演員\n"
    result += ", ".join(movie_analysis.highest_avg_revenue_actor()) + "\n"

    result += "第三題 : 艾瑪華森電影的平均分數是多少？\n"
    result += str(movie_analysis.avg_rating_for_emma_watson()) + "\n"

    result += "前三名導演" + "\n"
    top_directors, all_directors_rank = movie_analysis.top_directors_with_collaborations()
    for director in top_directors:
        result += director + "\n"

    result += "第五題 : 出演最多類型電影的前兩名演員\n"
    count = 1
    for actor in movie_analysis.top2_actors_with_most_genres():
        result += str(count) + ". " + actor + "\n"
        count += 1

    result += "第六題 : 電影中年數差距最大的前三名演員\n"
    count = 1
    for actor in movie_analysis.top3_actors_with_max_year_gap():
        result += str(count) + ". " + actor + "\n"
        count += 1

    result += "第七題 : 尋找所有與強尼戴普直接和間接合作的演員\n"
    count = 1
    for actor in movie_analysis.actors_cooperated_with_johnny_depp_2():
        result += str(count) + ". " + actor + "\n"
        count += 1

    # FileParser.file_append(result, output_path)
    print(result)

main()
