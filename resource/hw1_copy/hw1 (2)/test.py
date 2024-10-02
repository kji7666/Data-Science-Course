def gol_step(board):
    if not is_valid_board(board):
        return None
    
    output = np.zeros_like(board) 
    for y in range(board.shape[0]):
        pos = np.array((y, 0))
        for x in range(board.shape[1]):
            pos[1] = x
            live_neigbor = 0
            for delta in np.array((
                (-1, -1), (-1, 0), (-1, 1), 
                ( 0, -1),          ( 0, 1),
                ( 1, -1), ( 1, 0), ( 1, 1)
            )) : 
                out_pos = pos + delta
                live_neigbor += board[tuple(out_pos % a.shape)]

            if live_neigbor == 2: # live
                output[y, x] = board[y, x]
            elif live_neigbor == 3:
                output[y, x] = 1

    return output