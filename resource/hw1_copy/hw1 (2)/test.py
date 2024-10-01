import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

# 讀取資料
data = pd.read_csv('cdata.csv')
X = data[['x', 'y']].values  # 取出 X 和 Y 作為特徵

# 設定 K 值
K = 4
max_iters = 100
tol = 1e-4  # 收斂閾值

# 隨機選擇初始質心
np.random.seed(42)
centroids = X[np.random.choice(X.shape[0], K, replace=False)]

def compute_distance(X, centroids):
    """計算每個點到每個質心的距離"""
    return np.sqrt(((X[:, np.newaxis] - centroids) ** 2).sum(axis=2))

def update_centroids(X, labels, K):
    """更新每個類別的質心"""
    new_centroids = np.array([X[labels == k].mean(axis=0) for k in range(K)])
    return new_centroids

def plot_clusters(X, labels, centroids, iteration):
    """繪製聚類結果和質心"""
    plt.figure()
    for k in range(K):
        cluster_points = X[labels == k]
        plt.scatter(cluster_points[:, 0], cluster_points[:, 1], label=f'Cluster {k+1}')
    plt.scatter(centroids[:, 0], centroids[:, 1], s=300, c='red', marker='X', label='Centroids')
    plt.title(f'Iteration {iteration}')
    plt.legend()
    plt.show()

# 開始 K-means 聚類
for i in range(max_iters):
    # 計算每個點到質心的距離
    distances = compute_distance(X, centroids)
    
    # 分配類別標籤，選擇最接近的質心
    labels = np.argmin(distances, axis=1)
    
    # 繪製每次迭代的聚類結果
    plot_clusters(X, labels, centroids, i+1)
    
    # 計算新的質心
    new_centroids = update_centroids(X, labels, K)
    
    # 檢查質心的變化量是否小於閾值，判斷是否收斂
    if np.all(np.abs(new_centroids - centroids) < tol):
        print(f'Algorithm converged after {i+1} iterations.')
        break
    
    # 更新質心
    centroids = new_centroids