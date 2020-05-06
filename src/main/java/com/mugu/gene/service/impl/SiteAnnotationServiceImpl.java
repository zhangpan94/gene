package com.mugu.gene.service.impl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.mugu.gene.common.GeneFileReader;
import com.mugu.gene.common.GeneType;
import com.mugu.gene.controller.MediaTypeUtils;
import com.mugu.gene.model.CountGene;
import com.mugu.gene.model.Region;
import com.mugu.gene.service.AsyncService;
import com.mugu.gene.service.SiteAnnotationService;
import com.mugu.gene.util.FileReadUtil;
import com.mugu.gene.util.ListUtils;
import com.mugu.gene.util.StrUtils;
import com.mugu.gene.web.Result;
import com.mugu.gene.web.ResultMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @Author : zp
 * @Description :
 * @Date : 2019/10/17
 */

@Service
public class SiteAnnotationServiceImpl implements SiteAnnotationService {

    private Log log = LogFactory.get();

    @Autowired
    private ServletContext servletContext;

    @Value("${com.mugu.filePath}")
    private String filePath;

    @Value("${com.mugu.snpMapfile}")
    private String snpMapfile;

    @Value("${com.mugu.outFileName}")
    private String outFileName;

    @Value("${com.mugu.upload}")
    private String uploadFile;

    @Autowired
    AsyncService asyncService;


    @Override
    public String getSiteAnnotation() throws IOException {
        return null;
    }

    @Override
    public Result getSiteAnnotationFromFile(MultipartFile file, String pvalue, String str) {
        log.info("into getSiteAnnotationFromFile value:{}" + pvalue);
        if (!StringUtils.isEmpty(pvalue) && StrUtils.isNotStrDou(pvalue)) {
            return Result.failed("p值错误", ResultMsg.PARM_ERROR);
        }
        try {
            CountGene countGene = new CountGene();
            List<Integer> arr = new ArrayList<>();
            List<Region> regions = GeneFileReader.regionsRead(filePath + snpMapfile);
            Map<String, List<Region>> whichGene = GeneFileReader.findWhichGene(regions);
            Map<String, Map<Integer, String>> stringMapMap = GeneFileReader.stringMapMap(whichGene, arr);
            int five_prime_UTR = 0;
            int three_prime_UTR = 0;
            int promoter = 0;
            int exon = 0;
            int intron = 0;
            int repeat = 0;
            int intergenic = 0;
            File runFile;
            if (!StringUtils.isEmpty(file)) {
                String filename = file.getOriginalFilename();
                runFile = new File(uploadFile + filename);
                file.transferTo(runFile);
            } else if (StringUtils.isEmpty(file) && !StringUtils.isEmpty(str)) {
                File tmpFile = new File(filePath + FileReadUtil.generateShortUuid());
                FileWriter fw = new FileWriter(tmpFile);
                fw.write(str);
                fw.flush();
                fw.close();
                runFile = tmpFile;
            } else {
                return Result.failed("error");
            }
            BufferedReader bufr = new BufferedReader(new FileReader(runFile));
            String line;
            while ((line = bufr.readLine()) != null) {
                String[] split = line.split("\t");
                if (StrUtils.isNotStrNum(split[4]) || split.length != 10 || StrUtils.isNotStrDou(split[8])) {
                    continue;
                }
                boolean flag = !StringUtils.isEmpty(pvalue) && Double.valueOf(split[8]) < Double.valueOf(pvalue);
                if (flag || StringUtils.isEmpty(pvalue)) {
                    Map<Integer, String> integerStringMap = stringMapMap.get(split[0]);
                    int bp = Integer.parseInt(split[4]);
                    Integer reginre = ListUtils.recursionBinarySearch(arr, bp, 0, arr.size() - 1);
                    if (reginre == -1) {
                        intergenic += 1;
                        continue;
                    }
                    if (integerStringMap == null) {
                        intergenic += 1;
                        continue;
                    }
                    String type = integerStringMap.get(reginre);
                    if (GeneType.FIVE.equals(type)) {
                        five_prime_UTR = five_prime_UTR + 1;
                    } else if (GeneType.THREE.equals(type)) {
                        three_prime_UTR = three_prime_UTR + 1;
                    } else if (GeneType.PRO.equals(type)) {
                        promoter = promoter + 1;
                    } else if (GeneType.EXON.equals(type)) {
                        exon = exon + 1;
                    } else if (GeneType.INT.equals(type)) {
                        intron = intron + 1;
                    } else if (GeneType.REP.equals(type)) {
                        repeat = repeat + 1;
                    } else {
                        intergenic = intergenic + 1;
                    }
                }
            }
            bufr.close();
            countGene.setFive_prime_UTR(five_prime_UTR);
            countGene.setThree_prime_UTR(three_prime_UTR);
            countGene.setPromoter(promoter);
            countGene.setExon(exon);
            countGene.setIntron(intron);
            countGene.setRepeat(repeat);
            countGene.setIntergenic(intergenic);
            return Result.succeed(countGene, ResultMsg.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("fail to read file");
            return Result.failed("文件处理异常", ResultMsg.PARM_ERROR);
        }
    }

    @Override
    public Result getSnpMapAnalysisFile(MultipartFile file) {
        String uuid = getSnpFile(file);
        String fileUploadName = filePath + File.separator + "upload" + File.separator + uuid + File.separator + file.getOriginalFilename();
        String fileResultName = filePath + File.separator + "result" + File.separator + uuid + File.separator + outFileName;
        asyncService.generateResult(fileUploadName, fileResultName);
        return Result.succeed(uuid, ResultMsg.SUCCESS);
    }

    @Override
    public Result getSnpMapAnalysisOut(String id, HttpServletResponse response) {
        log.info("enter getSnpMapAnalysisOut id :" + id);
        String outfilePath = filePath + File.separator + "result" + File.separator + id + File.separator + outFileName;
        MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext, outFileName);
        File file = new File(outfilePath);
        if (file.exists()) {
            response.setContentType(mediaType.getType());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName());
            response.setContentLength((int) file.length());
            try {
                BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));
                BufferedOutputStream outStream = new BufferedOutputStream(response.getOutputStream());
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
                outStream.flush();
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return Result.failed("文件未解析完", ResultMsg.SUCCESS);
        }
        log.info("download getSnpMapAnalysisOut finshed");
        return null;
    }

    @Override
    public Result getEqtlSnpFile(MultipartFile uploadFile, String organization) {
        log.info("enter getSnpMapAnalysisOut id :" + uploadFile.getName());

        String uuid = getSnpFile(uploadFile);
        String fileUploadName = filePath + File.separator + "upload" + File.separator + uuid + File.separator + uploadFile.getOriginalFilename();
        String fileResultName = filePath + File.separator + "result" + File.separator + uuid + File.separator + outFileName;
        asyncService.generateSnpResult(fileUploadName, fileResultName, organization);
        return Result.succeed(uuid, ResultMsg.SUCCESS);
    }


    private String getSnpFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        System.out.println("报表线程名称：【" + Thread.currentThread().getName() + "】");
        String fileUploadName = filePath + File.separator + "upload" + File.separator;
        String fileResultName = filePath + File.separator + "result" + File.separator;
        String f1ileUploadName = fileUploadName + uuid + File.separator;
        String f1ileResultName = fileResultName + uuid + File.separator;
        File files = new File(f1ileUploadName);
        File out = new File(f1ileResultName);
        if (!files.exists()) {
            files.mkdir();
        }
        if (!out.exists()) {
            out.mkdir();
        }
        try {
            file.transferTo(new File(f1ileUploadName + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uuid;
    }

}
