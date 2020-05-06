package com.mugu.gene.service.impl;

import com.mugu.gene.common.FullSnpFileRead;
import com.mugu.gene.common.GeneFileReader;
import com.mugu.gene.service.AsyncService;
import com.mugu.gene.service.EqtlRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @Author : zp
 * @Description :
 * @Date : 2019/11/4
 */
@Service
@Slf4j
public class AsyncServiceImpl implements AsyncService {

    @Value("${com.mugu.m6aMap}")
    private String snpMapFilePath;
    @Value("${com.mugu.filePath}")
    private String filePath;
    @Value("${com.mugu.gtexPath}")
    private String gtexPath;

    @Autowired
    private EqtlRepository eqtlRepository;

    @Override
    @Async
    public void generateResult(String file, String fileout) {
        log.info("generateResult线程名称：【" + Thread.currentThread().getName() + "】");
        try {
            Map<String, List<String>> map = GeneFileReader.parseSnpToMap(filePath + snpMapFilePath);
            GeneFileReader.parseSnp(map, file, fileout);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("exit generateResult");
    }

    @Override
    @Async
    public void generateSnpResult(String file, String fileout, String organization) {
        log.info("generateSnpResult线程名称：" + Thread.currentThread().getName());
        String organPath = gtexPath + organization + ".signifpairs.txt";
        try {
            Map<String, List<String>> map = FullSnpFileRead.readFullSnpFileToMap(organPath);
            BufferedReader buffer = new BufferedReader(new FileReader(new File(file)));
            FileOutputStream outputStream = new FileOutputStream(new File(fileout));
            String strLine = null;
            while (null != (strLine = buffer.readLine())) {
                String[] split = strLine.split("\t");
                String str = split[0] + "_" + split[4] + "_" + split[2] + "_" + split[3];
                String variantId = str.replace("chr", "");
                if ("hg19chrc".equals(split[0])) {
                    String out = strLine + "\tgeneId\ttssDistance\tmaSamples\tmaCount\tmaf\tpvalNominal\tslope\tslopeSe\tpvalNominalThreshold\tminPvalNominal\tpvalBeta\n";
                    outputStream.write(out.getBytes());
                    continue;
                }
                List<String> byVariantId = map.get(variantId);
                if (StringUtils.isEmpty(byVariantId)) {
                    outputStream.write((strLine + "\n").getBytes());
                    continue;
                }
                for (String eqtlEntity : byVariantId) {
                    String out = strLine + "\t" + eqtlEntity + "\n";
                    outputStream.write(out.getBytes());
                }
            }
            buffer.close();
            outputStream.close();
            log.info("end");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
