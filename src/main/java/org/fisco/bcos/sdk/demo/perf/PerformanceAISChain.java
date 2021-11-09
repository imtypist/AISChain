package org.fisco.bcos.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.demo.perf.callback.PerformanceCallback;
import org.fisco.bcos.sdk.demo.perf.collector.PerformanceCollector;
import org.fisco.bcos.sdk.model.ConstantConfig;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.utils.ThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceAISChain {
    private static Logger logger = LoggerFactory.getLogger(PerformanceAISChain.class);
    private static AtomicInteger sendedTransactions = new AtomicInteger(0);

    public static final String[] ABI_ARRAY = {
        "[{\"constant\":true,\"inputs\":[],\"name\":\"getAISData\",\"outputs\":[{\"components\":[{\"name\":\"sender\",\"type\":\"address\"},{\"name\":\"timestamp\",\"type\":\"string\"},{\"name\":\"shipid\",\"type\":\"string\"},{\"name\":\"lon\",\"type\":\"string\"},{\"name\":\"lat\",\"type\":\"string\"},{\"name\":\"heading\",\"type\":\"string\"},{\"name\":\"course\",\"type\":\"string\"},{\"name\":\"speed\",\"type\":\"string\"},{\"name\":\"shiptype\",\"type\":\"string\"},{\"name\":\"destination\",\"type\":\"string\"}],\"name\":\"\",\"type\":\"tuple[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"ts\",\"type\":\"string\"},{\"name\":\"shipid\",\"type\":\"string\"},{\"name\":\"lon\",\"type\":\"string\"},{\"name\":\"lat\",\"type\":\"string\"},{\"name\":\"heading\",\"type\":\"string\"},{\"name\":\"course\",\"type\":\"string\"},{\"name\":\"speed\",\"type\":\"string\"},{\"name\":\"shiptype\",\"type\":\"string\"},{\"name\":\"destination\",\"type\":\"string\"}],\"name\":\"addAISData\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = String.join("", ABI_ARRAY);

    private static void Usage() {
        System.out.println(" Usage:");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceAISChain [count] [qps] [groupId] [path_to_ais_csv].");
    }

    public static void main(String[] args) {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = PerformanceAISChain.class.getClassLoader().getResource(configFileName);

            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }
            if (args.length < 4) {
                Usage();
                return;
            }
            Integer count = Integer.valueOf(args[0]);
            Integer qps = Integer.valueOf(args[1]);
            Integer groupId = Integer.valueOf(args[2]);
            String AISFile = String.valueOf(args[3]);
            System.out.println(
                    "====== PerformanceAISChain trans, count: "
                            + count
                            + ", qps:"
                            + qps
                            + ", groupId: "
                            + groupId
                            + ", AISFile: "
                            + AISFile);

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);

            // build the client
            Client client = sdk.getClient(groupId);

            // 构造AssembleTransactionProcessor对象，需要传入client对象，CryptoKeyPair对象和abi、binary文件存放的路径。abi和binary文件需要在上一步复制到定义的文件夹中。
            CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();
            AssembleTransactionProcessor transactionProcessor =
                    TransactionProcessorFactory.createAssembleTransactionProcessor(
                            client,
                            keyPair,
                            "~/java-sdk-demo/src/main/resources/abi/",
                            "~/java-sdk-demo/src/main/resources/bin/");

            // deploy the AISChain
            System.out.println("====== Deploy AISChain ====== ");
            // 部署AISChain合约。第一个参数为合约名称，第二个参数为合约构造函数的列表，是List<Object>类型。
            TransactionResponse response =
                    transactionProcessor.deployByContractLoader("AISChain", new ArrayList<>());
            String AISChainAddress = response.getContractAddress();
            System.out.println(
                    "====== Deploy AISChain success, address: " + AISChainAddress + " ====== ");

            // read AIS data from file
            BufferedReader br = new BufferedReader(new FileReader(AISFile));
            br.readLine(); // skip the first line
            // ArrayList<String> AISData = new ArrayList<>();
            String singleAISData;
            singleAISData = br.readLine();
            String[] params = singleAISData.split(",");
            // while ((singleAISData = br.readLine()) != null) {
            //     AISData.add(singleAISData);
            // }

            PerformanceCollector collector = new PerformanceCollector();
            collector.setTotal(count);
            RateLimiter limiter = RateLimiter.create(qps);
            Integer area = count / 10;
            final Integer total = count;

            System.out.println("====== PerformanceAISChain trans start ======");

            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "PerformanceAISChain",
                            sdk.getConfig().getThreadPoolConfig().getMaxBlockingQueueSize());

            for (Integer i = 0; i < count; ++i) {
                limiter.acquire();
                // int index = i % AISData.size();
                threadPoolService
                        .getThreadPool()
                        .execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        PerformanceCallback callback = new PerformanceCallback();
                                        callback.setTimeout(0);
                                        callback.setCollector(collector);
                                        // String[] params = AISData.get(index).split(",");
                                        // TODO: BFT replicated detection here
                                        try {
                                            transactionProcessor.sendTransactionAsync(
                                                    AISChainAddress,
                                                    ABI,
                                                    "addAISData",
                                                    Arrays.asList(params),
                                                    callback);
                                        } catch (Exception e) {
                                            TransactionReceipt receipt = new TransactionReceipt();
                                            receipt.setStatus("-1");
                                            callback.onResponse(receipt);
                                            logger.info(e.getMessage());
                                        }
                                        int current = sendedTransactions.incrementAndGet();
                                        if (current >= area && ((current % area) == 0)) {
                                            System.out.println(
                                                    "Already sended: "
                                                            + current
                                                            + "/"
                                                            + total
                                                            + " transactions");
                                        }
                                    }
                                });
            }
            // wait to collect all the receipts
            while (!collector.getReceived().equals(count)) {
                Thread.sleep(1000);
            }
            threadPoolService.stop();
            System.exit(0);
        } catch (Exception e) {
            System.out.println(
                    "====== PerformanceAISChain test failed, error message: " + e.getMessage());
            System.exit(0);
        }
    }
}
