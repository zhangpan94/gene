package com.mugu.gene.controller;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.mugu.gene.model.EqtlEntity;
import com.mugu.gene.service.EqtlRepository;
import com.mugu.gene.service.SiteAnnotationService;
import com.mugu.gene.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author : zp
 * @Description :
 * @Date : 2019/10/17
 */

@RestController
@RequestMapping("/")
public class SiteAnalysisController {

    private Log log = LogFactory.get();

    @Autowired
    private SiteAnnotationService annotationService;

    @RequestMapping(value = "/siteannotation", method = RequestMethod.POST)
    public Result getSiteAnnotation(@RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile,
                                    @RequestParam(value = "pvalue", required = false) String pvalue,
                                    @RequestParam(value = "snpId", required = false) String snpId) {
        log.info("enter into siteannotation");
        return annotationService.getSiteAnnotationFromFile(uploadFile, pvalue, snpId);
    }

    @RequestMapping(value = "/snpMapAnalysis", method = RequestMethod.POST)
    public Result getSnpMapAnalysisFile(@RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile) {
        log.info("enter into siteannotation");
        return annotationService.getSnpMapAnalysisFile(uploadFile);
    }

    @RequestMapping(value = "/eqtlSnpFile", method = RequestMethod.POST)
    public Result getEqtlSnpFile(@RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile,
                                 @RequestParam(value = "organization", required = false) String organization) {
        log.info("enter into siteannotation");
        return annotationService.getEqtlSnpFile(uploadFile, organization);
    }

    @RequestMapping(value = "/downLoadResult", method = RequestMethod.GET)
    public Result getSnpMapAnalysisOut(@RequestParam(value = "id") String id, HttpServletResponse response) {
        return annotationService.getSnpMapAnalysisOut(id, response);
    }

}
