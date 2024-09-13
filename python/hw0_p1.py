# 定義state(若是只需要output一種表示法, 可啟用state)
WITHOUT_SYMBOLS = 0 
WITH_SYMBOLS = 1

# 初始化狀態為without
state = WITHOUT_SYMBOLS

def main():
    input_polynomial = input("Input the polynomials: ") # input("前置詞") + 輸入
    result = multiply_polynomials(input_polynomial) 
    # state WITH_SYMBOLS
    print("Output Result : " + result)
    # state WITHOUT_SYMBOLS
    print("Bonus Output Result : " + remove_symbols(result))

def multiply_polynomials(polynomial_string):
    determine_state(polynomial_string)
    # 去除多項式中的符號(格式化 eg. 2X2Y2)
    polynomial_string = remove_symbols(polynomial_string)
    # mapping (var => coff)
    result_map = parse_polynomial(polynomial_string)
    # 格式化output
    result = format_output(result_map)
    return result

def determine_state(polynomial_string):
    global state
    # 根據多項式中是否包含'*'或'^'來設定狀態(若是只需要output一種表示法, 可啟用)
    if '*' in polynomial_string or '^' in polynomial_string:
        state = WITH_SYMBOLS
    else:
        state = WITHOUT_SYMBOLS

def remove_symbols(polynomial_string):
    result = ""
    # 遍歷多項式字符串，去除 '*' 和 '^' 符號
    for ch in polynomial_string:
        if ch not in ('*', '^'):
            result += ch
    return result

def parse_polynomial(polynomial_string):
    # 去除空格
    polynomial_string = polynomial_string.replace(" ", "")
    # 有括號 => 多個多項式相乘
    if "(" in polynomial_string: # in == contains
        polynomial_array = polynomial_string.replace(")", "").split("(")[1:] # [1:] == substring(1) 砍掉開頭的"("
    else:  # 單一多項式
        polynomial_array = [polynomial_string]
    
    variableMap_list = [] # =[] 即為 list, 不須宣告內部data type
    
    for polynomial in polynomial_array:
        variable_map = {}  # ={} 即為 mapping
        # 分割多項式的項
        terms = split_terms(polynomial)
        
        for term in terms: # for-each
            if not term: # not term == term.isEmpty()
                continue
            
            coefficient = 1 # 預設值
            is_negative = False
            variable_index = -1
            
            if term.startswith("-"):
                is_negative = True
                term = term[1:] # 截取數字
            
            # 確定變量的開始位置
            for i in range(len(term)): # for(int i=0; i<term.length().length; i++)
                if term[i].isalpha(): # isalpha == isletter
                    variable_index = i
                    break
            
            # 解析變量和係數
            if variable_index != -1: # 係數顯示 (-1, 1會不顯示"1")
                try:
                    coefficient = int(term[:variable_index]) #截係數
                except ValueError:
                    coefficient = 1
                
                if is_negative:
                    coefficient *= -1 #補正負
                
                variable = term[variable_index:] #截變量
                variable_map[variable] = coefficient # == put(variable, coefficient)
            else: # 係數不顯示
                try:
                    coefficient = int(term)
                    if is_negative:
                        coefficient *= -1
                    variable_map[""] = coefficient
                except ValueError:
                    print(f"Invalid term format: {term}")
        
        variableMap_list.append(variable_map) # add
    
    return multiplication(variableMap_list)

def split_terms(polynomial):
    terms = [] #list
    current_term = ""
    
    for i, ch in enumerate(polynomial): # enumerate() 函數遍歷 polynomial 字符串中的每個字符 ch，同時獲取字符的索引 i
        if ch == '+' and i > 0: # and == &&
            terms.append(current_term) # append == add
            current_term = ""
        elif ch == '-' and i > 0:
            terms.append(current_term)
            current_term = "-"
        else:
            current_term += ch # +-為界
    
    terms.append(current_term)
    return terms

def multiplication(variableMap_list):
    while len(variableMap_list) > 1: # 兩兩多項式相乘, 產出新多項式後加到最後, 移除剛剛運算的兩多項式, 只剩一個代表全部乘完
        result_map = {}
        first_polynomial = variableMap_list.pop(0)
        second_polynomial = variableMap_list.pop(0)
        
        for var1, coeff1 in first_polynomial.items():
            for var2, coeff2 in second_polynomial.items():
                new_variable = multiply_variables(var1, var2)
                new_coefficient = coeff1 * coeff2
                if new_variable in result_map:
                    result_map[new_variable] += new_coefficient
                else:
                    result_map[new_variable] = new_coefficient
        
        variableMap_list.insert(0, result_map)
    
    return variableMap_list[0]

def multiply_variables(var1, var2):
    return expand_polynomial(var1 + var2)

def expand_polynomial(polynomial): # 乘法方式舉例 : 2X2Y2 * 3AX2 => 展開 : 2XXYY * 3AXX => 6XXYYAXX, 此為展開用function
    result = []
    i = 0
    length = len(polynomial)
    
    while i < length:
        ch = polynomial[i]
        if ch.isalpha(): # 如果後續是字母, 繼續收集這些字母, 直到遇到非字母
            variable = ch
            i += 1
            number = ""
            
            while i < length and polynomial[i].isdigit(): # 如果後續是數字, 繼續收集這些數字, 直到遇到非數字
                number += polynomial[i]
                i += 1
            
            count = int(number) if number else 1 # 次方, 如果沒有數字, 默認為 1。
            
            for _ in range(count):
                result.append(variable) # 根據次數，將變量添加到 result
        else:
            i += 1
    
    return ''.join(result) # 將 result 列表中的字符合併為一個字符串並返回

def format_output(result_map): # 拼出output
    record_map = {}
    
    for variable, coefficient in result_map.items():
        power_map = {} # XXXYY => 轉回 X3Y2
        
        for ch in variable: # 數次數
            if ch in power_map:
                power_map[ch] += 1
            else:
                power_map[ch] = 1
        
        power_map = sort_dict_by_key(power_map) # XY等未知數 要排序, 等等才能做同項合併

        formatted_variable = ''
        for var, exp in power_map.items():
            if exp > 1:
                formatted_variable += f"{var}^{exp}"
            else:
                formatted_variable += var
        
        if formatted_variable in record_map: # 同項合併, 丟到新map
            record_map[formatted_variable] += coefficient
        else:
            record_map[formatted_variable] = coefficient
    
    result = []
    for variable, coefficient in record_map.items(): # 各項串接時須注意(1/-1/負數)的表現方式
        if coefficient < -1:
            result.append(f"{coefficient}*{variable}")
        elif coefficient == -1:
            result.append(f"-{variable}")
        elif coefficient == 1:
            result.append(f"+{variable}")
        else:
            result.append(f"+{coefficient}*{variable}")
    
    return ''.join(result).lstrip('+') # 將最終的多項式結果格式化為一個字符串，並移除字符串開頭的加號

def sort_dict_by_key(dictionary):
    # 使用 sorted() 根據鍵進行排序
    sorted_items = sorted(dictionary.items(), key=lambda item: item[0])
    # 將排序後的鍵值對轉換為字典
    sorted_dict = dict(sorted_items)
    return sorted_dict

# 調用主函數
main()
