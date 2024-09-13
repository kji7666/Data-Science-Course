WITHOUT_SYMBOLS = 0
WITH_SYMBOLS = 1
state = WITHOUT_SYMBOLS

def main():
    input_polynomial = input("Input the polynomials: ")
    result = multiply_polynomials(input_polynomial)
    print("Output Result : " + result)
    print("Bonus Output Result : " + remove_symbols(result))

def multiply_polynomials(polynomial_string):
    determine_state(polynomial_string)
    polynomial_string = remove_symbols(polynomial_string)
    result_map = parse_polynomial(polynomial_string)
    result = format_output(result_map)
    return result

def determine_state(polynomial_string):
    global state
    if '*' in polynomial_string or '^' in polynomial_string:
        state = WITH_SYMBOLS
    else:
        state = WITHOUT_SYMBOLS

def remove_symbols(polynomial_string):
    result = ""
    for ch in polynomial_string:
        if ch not in ('*', '^'):
            result += ch
    return result

def parse_polynomial(polynomial_string):
    polynomial_string = polynomial_string.replace(" ", "")  # 移除空格
    if "(" in polynomial_string:  # 多個多項式相乘
        polynomial_array = polynomial_string.replace(")", "").split("(")[1:]
    else:  # 單一多項式
        polynomial_array = [polynomial_string]
    
    variable_list = []
    
    for polynomial in polynomial_array:
        variable_map = {}
        terms = split_terms(polynomial)
        
        for term in terms:
            if not term:
                continue
            
            coefficient = 1
            is_negative = False
            variable_index = -1
            
            if term.startswith("-"):
                is_negative = True
                term = term[1:]
            
            for i in range(len(term)):
                if term[i].isalpha():
                    variable_index = i
                    break
            
            if variable_index != -1:
                try:
                    coefficient = int(term[:variable_index])
                except ValueError:
                    coefficient = 1
                
                if is_negative:
                    coefficient *= -1
                
                variable = term[variable_index:]
                variable_map[variable] = coefficient
            else:
                try:
                    coefficient = int(term)
                    if is_negative:
                        coefficient *= -1
                    variable_map[""] = coefficient
                except ValueError:
                    print(f"Invalid term format: {term}")
        
        variable_list.append(variable_map)
    
    return multiplication(variable_list)

def split_terms(polynomial):
    terms = []
    current_term = ""
    
    for i, ch in enumerate(polynomial):
        if ch == '+' and i > 0:
            terms.append(current_term)
            current_term = ""
        elif ch == '-' and i > 0:
            terms.append(current_term)
            current_term = "-"
        else:
            current_term += ch
    
    terms.append(current_term)
    return terms

def multiplication(variable_list):
    while len(variable_list) > 1:
        result_map = {}
        first_polynomial = variable_list.pop(0)
        second_polynomial = variable_list.pop(0)
        
        for var1, coeff1 in first_polynomial.items():
            for var2, coeff2 in second_polynomial.items():
                new_variable = multiply_variables(var1, var2)
                new_coefficient = coeff1 * coeff2
                if new_variable in result_map:
                    result_map[new_variable] += new_coefficient
                else:
                    result_map[new_variable] = new_coefficient
        
        variable_list.insert(0, result_map)
    
    return variable_list[0]

def multiply_variables(var1, var2):
    return expand_polynomial(var1 + var2)

def expand_polynomial(polynomial):
    result = []
    i = 0
    length = len(polynomial)
    
    while i < length:
        ch = polynomial[i]
        if ch.isalpha():
            variable = ch
            i += 1
            number = ""
            
            while i < length and polynomial[i].isdigit():
                number += polynomial[i]
                i += 1
            
            count = int(number) if number else 1
            
            for _ in range(count):
                result.append(variable)
        else:
            i += 1
    
    return ''.join(result)

def format_output(result_map):
    record_map = {}
    
    for variable, coefficient in result_map.items():
        power_map = {}
        
        for ch in variable:
            if ch in power_map:
                power_map[ch] += 1
            else:
                power_map[ch] = 1
        
        power_map = sort_dict_by_key(power_map)

        formatted_variable = ''
        for var, exp in power_map.items():
            if exp > 1:
                formatted_variable += f"{var}^{exp}"
            else:
                formatted_variable += var
        
        if formatted_variable in record_map:
            record_map[formatted_variable] += coefficient
        else:
            record_map[formatted_variable] = coefficient
    
    result = []
    for variable, coefficient in record_map.items():
        if coefficient < -1:
            result.append(f"{coefficient}*{variable}")
        elif coefficient == -1:
            result.append(f"-{variable}")
        elif coefficient == 1:
            result.append(f"+{variable}")
        else:
            result.append(f"+{coefficient}*{variable}")
    
    return ''.join(result).lstrip('+')

def sort_dict_by_key(dictionary):
    # 使用 sorted() 根據鍵進行排序
    sorted_items = sorted(dictionary.items(), key=lambda item: item[0])
    # 將排序後的鍵值對轉換為字典
    sorted_dict = dict(sorted_items)
    return sorted_dict

main()