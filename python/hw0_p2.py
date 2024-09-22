class FileParser:
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
    def __init__(self, file_name):
        self.line_list = FileParser.file_reader(file_name)  # 讀整個文件 切成list # lines
        self.field_names = FileParser.field_name_getter(self.line_list)  # 取欄位名 # field
        self.data_list = FileParser.data_getter(self.line_list, self.field_names)  # 整理數據 # line list => field mapping data

    def get_line_list(self):
        return self.line_list

    def get_data_list(self):
        return self.data_list

    def get_field_names(self):
        return self.field_names

    # 根據指定欄位進行排序
    def sort_by_value(self, field_name, is_reverse_order):
        if field_name in self.field_names:
            try:
                # 將字符串轉為浮點數進行排序
                self.data_list.sort(key=lambda data_line_map: float(data_line_map.get(field_name, 0)), 
                                    reverse=is_reverse_order)
            except ValueError:
                print(f"欄位 '{field_name}' 不是數字類型，無法排序")

class MovieAnalysis:
    def __init__(self, file_name):
        self.dao = DataDao(file_name)

    def top3_movies_2016(self):
        # 從資料中篩選出2016年的電影並按收視率由高到低排序
        sorted_movies_2016 = sorted(
            (movie for movie in self.dao.get_data_list() if movie["Year"] == "2016"),
            key=lambda movie: float(movie["Rating"]),
            reverse=True
        )

        top_movies = []  # 存放前3名的電影
        min_rating_of_top3 = None  # 記錄第3名的最低評分

        for movie in sorted_movies_2016:
            rating = float(movie["Rating"])

            # 如果還沒選滿3部電影，或者當前電影評分等於第3名的評分，則加入結果
            if len(top_movies) < 3 or rating == min_rating_of_top3:
                top_movies.append(movie["Title"])

                # 如果加入的是第3部電影，記錄下第3名的評分
                if len(top_movies) == 3:
                    min_rating_of_top3 = rating
            else:
                break

        return top_movies


    def highest_avg_revenue_actor(self):
        # 用於儲存每位演員的收入
        actor_revenue_map = {}

        # 遍歷所有電影資料
        for movie in self.dao.get_data_list():
            actors = [actor.strip() for actor in movie["Actors"].split("|")]
            revenue_str = movie.get("Revenue (Millions)")

            # 如果收入資料有效，則記錄每位演員的收入
            if revenue_str:
                try:
                    revenue = float(revenue_str)
                    for actor in actors:
                        # 使用 setdefault 來初始化演員的收入列表
                        actor_revenue_map.setdefault(actor, []).append(revenue)
                except ValueError:
                    print(f"Invalid revenue data: {revenue_str}")

        # 計算每位演員的平均收入
        actor_avg_revenue_map = {
            actor: sum(revenues) / len(revenues) for actor, revenues in actor_revenue_map.items()
        }

        # 找出最高的平均收入
        highest_avg_revenue = max(actor_avg_revenue_map.values(), default=0)

        # 找出所有平均收入等於最高平均收入的演員
        highest_avg_revenue_actors = [
            actor for actor, avg_revenue in actor_avg_revenue_map.items() if avg_revenue == highest_avg_revenue
        ]

        return highest_avg_revenue_actors


    # 計算艾瑪華森電影的平均分數
    def avg_rating_for_emma_watson(self):
        # 找出包含艾瑪華森的電影
        movies_with_emma = [movie for movie in self.dao.get_data_list() if "Emma Watson" in movie["Actors"]]
        
        # 提取這些電影的評分
        ratings = []
        for movie in movies_with_emma:
            try:
                rating = float(movie["Rating"])
                ratings.append(rating)
            except ValueError:
                print(f"無效的評分數據: {movie['Rating']}")  # 如果評分無法轉換為浮點數，則輸出錯誤信息

        # 計算平均評分
        if ratings:  # 如果有有效的評分
            average_rating = sum(ratings) / len(ratings)
            return average_rating
        else:
            return 0  # 如果沒有評分，則返回0

    # 與最多不同演員合作的前三名導演及所有導演的排行榜
    def top_directors_with_collaborations(self):
        director_actors_map = {}

        for movie in self.dao.get_data_list():
            director = movie["Director"]
            actors = set(actor.strip() for actor in movie["Actors"].split("|"))  # 使用 set 確保演員不重複
            if director in director_actors_map:
                director_actors_map[director].update(actors)
            else:
                director_actors_map[director] = actors

        sorted_directors = sorted(
            director_actors_map.items(),
            key=lambda entry: len(entry[1]),  # 根據合作演員數量排序
            reverse=True
        )

        top_directors = []
        count = 0
        min_collaborations_of_top3 = -1

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


    def top2_actors_with_most_genres(self):
        actor_genres_map = {}

        # 收集演員及其演出的不同類型
        for movie in self.dao.get_data_list():
            actors = [actor.strip() for actor in movie["Actors"].split("|")]  # 去除空格
            genres = set(movie["Genre"].split("|"))  # 使用 set 自動去重
            for actor in actors:
                if actor in actor_genres_map:
                    actor_genres_map[actor].update(genres)
                else:
                    actor_genres_map[actor] = set(genres)

        # 根據不同類型數量排序
        sorted_actors = sorted(
            actor_genres_map.items(),
            key=lambda entry: len(entry[1]),
            reverse=True
        )

        top_actors = []
        count = 0
        min_genres_of_top2 = -1

        # 找出前兩名演員
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


    def top3_actors_with_max_year_gap(self):
        actor_year_map = {}

        # 收集演員及其出演電影的年份
        for movie in self.dao.get_data_list():
            actors = [actor.strip() for actor in movie["Actors"].split("|")]  # 去除空格
            year = int(movie["Year"])
            for actor in actors:
                actor_year_map.setdefault(actor, []).append(year)

        # 計算每位演員的年份差距
        actor_year_gap_list = [
            (actor, max(years) - min(years))
            for actor, years in actor_year_map.items() if len(years) > 1  # 確保演員有多部電影
        ]

        # 根據年份差距排序
        sorted_actor_year_gap = sorted(actor_year_gap_list, key=lambda x: x[1], reverse=True)

        top_actors = []
        count = 0
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
        for actor, gap in top_actors:
            print(f"演員: {actor}, 年份差距: {gap} 年")

        return [actor for actor, gap in top_actors]



    def actors_cooperated_with_johnny_depp(self):
        cooperated_actors = set()
        actor_stack = ["Johnny Depp"]
        cooperated_actors.add("Johnny Depp")

        while actor_stack:
            current_actor = actor_stack.pop()
            for movie in self.dao.get_data_list():
                if current_actor in movie["Actors"]:
                    actors = [actor.strip() for actor in movie["Actors"].split("|") if actor.strip()]
                    
                    for actor in actors:
                        if actor not in cooperated_actors:
                            cooperated_actors.add(actor)
                            actor_stack.append(actor)

        return sorted(cooperated_actors)  # 按 ASCII 排序返回

def main():
    output_path = "python\\output.txt"
    movie_analysis = MovieAnalysis("python\\IMDB-Movie-Data.csv")

    # print("第一題 : 2016 年收視率最高的前三部電影的標題") # ok
    # FileParser.file_append("第一題 : 2016 年收視率最高的前三部電影的標題", output_path)
    
    # count = 1
    # for movie in movie_analysis.top3_movies_2016():
    #     print(f"{count}. {movie}")
    #     FileParser.file_append(f"{count}. {movie}", output_path)
    #     count += 1
    # FileParser.file_append("", output_path)

    # print("第二題 : 平均收入最高的演員")
    # FileParser.file_append("第二題 : 平均收入最高的演員", output_path)
    # print(movie_analysis.highest_avg_revenue_actor())
    # FileParser.file_append(", ".join(movie_analysis.highest_avg_revenue_actor()), output_path)
    # FileParser.file_append("", output_path)

    # print("第三題 : 艾瑪華森電影的平均分數是多少？")
    # FileParser.file_append("第三題 : 艾瑪華森電影的平均分數是多少？", output_path)
    # print(movie_analysis.avg_rating_for_emma_watson())
    # FileParser.file_append(str(movie_analysis.avg_rating_for_emma_watson()), output_path)
    # FileParser.file_append("", output_path)

    # top_directors, all_directors_rank = movie_analysis.top_directors_with_collaborations()

    # print("前三名導演：", top_directors)
    # print("所有導演排行榜：")
    # for director, actors in all_directors_rank:
    #     print(f"{director}: {len(actors)} 位演員")


    # print("第五題 : 出演最多類型電影的前兩名演員")
    # FileParser.file_append("第五題 : 出演最多類型電影的前兩名演員", output_path)
    # count = 1
    # for actor in movie_analysis.top2_actors_with_most_genres():
    #     print(f"{count}. {actor}")
    #     FileParser.file_append(f"{count}. {actor}", output_path)
    #     count += 1
    # FileParser.file_append("", output_path)

    # print("第六題 : 電影中年數差距最大的前三名演員")
    # FileParser.file_append("第六題 : 電影中年數差距最大的前三名演員", output_path)
    # count = 1
    # for actor in movie_analysis.top3_actors_with_max_year_gap():
    #     print(f"{count}. {actor}")
    #     FileParser.file_append(f"{count}. {actor}", output_path)
    #     count += 1
    # FileParser.file_append("", output_path)

    print("第七題 : 尋找所有與強尼戴普直接和間接合作的演員")
    FileParser.file_append("第七題 : 尋找所有與強尼戴普直接和間接合作的演員", output_path)
    count = 1
    for actor in movie_analysis.actors_cooperated_with_johnny_depp():
        print(f"{count}. {actor}")
        FileParser.file_append(f"{count}. {actor}", output_path)
        count += 1


main()
