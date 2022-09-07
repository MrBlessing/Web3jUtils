# Web3jUtils
Web3工具

# 注意事项
- 本人编程新人，如果对代码有什么建设性意见，欢迎提交你的代码
- 本项目因分享技术而生，如果你用到了本项目或者单纯喜欢，请不要吝啬你的star
- 如果有什么技术疑问，可以联系 oxthink@qq.com

# 基本使用
## 获取代币基本信息
```java
    public static void main(String[] args) throws Exception {
        Web3jUtil web3jUtil = new Web3jUtil(ChainInfo.BSC_MAIN);
        TokenUtil tokenUtil = new TokenUtil(web3jUtil);
        // 某个用户的的代币数量
        tokenUtil.balanceOf("代币合约","用户地址");
        // 代币名称
        tokenUtil.getName("代币合约");
        // 代币总供应量
        tokenUtil.getTotalSupply("代币合约");
    }
```
## 对代币进行操作
```java
    public static void main(String[] args) throws Exception {
        Web3jUtil web3jUtil = new Web3jUtil(ChainInfo.BSC_MAIN,"你的私钥");
        // 对代币进行无限授权
        web3jUtil.approveInfinite("代币地址","代币授权的地址");
        // 代币转账
        web3jUtil.transfer("代币地址","接收代币的地址","发送的代币数量");
        // eth等原生代币转账
        web3jUtil.sendEther("接收地址","发送数量");
    }
```
## 对合约进行读操作
```java
    public static void main(String[] args) throws Exception {
        Web3jUtil web3jUtil = new Web3jUtil(ChainInfo.BSC_MAIN);
        // 方法参数列表
        List input = Arrays.asList(new Address(""), new Uint256(new BigInteger("123", 16)));
        // 返回参数列表
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Uint256>() {});
        List<Type> result = web3jUtil.readContract("合约地址", "方法名", input, output);
        // 获取返回值
        BigInteger decimals = (BigInteger) result.get(0).getValue();
    }
```

## 对合约进行写操作
```java
    public static void main(String[] args) throws Exception {
        Web3jUtil web3jUtil = new Web3jUtil(ChainInfo.BSC_MAIN,"你的私钥");
        // 设置可以接受的gasPrice范围
        web3jUtil.setGasPriceLimit("最小的gasPrice","最大的GasPrice");
        // 方法参数列表
        List input = Arrays.asList(new Address(""), new Uint256(new BigInteger("123", 16)));
        // 返回参数列表
        List<TypeReference<?>> output = Collections.singletonList(new TypeReference<Uint256>() {});
        String hash = web3jUtil.writeContract("合约地址", "方法名", input, output);
        // 阻塞方法，直到该笔交易被完成
        boolean state = web3jUtil.blockTransactionUtilComplete(hash,等待的时间)；
    }
```
## 与Swap合约交互
```java
 public static void main(String[] args) throws Exception {
        Web3jUtil web3jUtil = new Web3jUtil(ChainInfo.BSC_MAIN, "你的私钥");
        // 设置可以接受的gasPrice范围
        web3jUtil.setGasPriceLimit("最小的gasPrice","最大的GasPrice");
        PancakeUtil pancakeUtil = new PancakeUtil();
        // 自动查找路径购买或卖出代币
        String hash = pancakeUtil.swapExactIn("卖出的代币地址", "卖出的代币数量", "买入的代币地址", "买入的代币最少接受数量", "可接受的滑点");
        // 手动输入路径购买或卖出代币
        List<String> path = new ArrayList<>();
        path.add("代币地址1");
        path.add("代币地址2");
        String hash1 = pancakeUtil.swapExactIn("卖出的代币地址", "卖出的代币数量",
                "买入的代币地址", "买入的代币最少接受数量",
                path,
                可接受的滑点);
        // 监控池子，并买入代币（不考虑滑点，无脑买入）
        pancakeUtil.rushToPurchase("输入的bnb数量","购买的代币地址",尝试次数);
    }
```


