package com.mugu.gene.service;

import com.mugu.gene.web.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author : zp
 * @Description :
 * @Date : 2019/10/17
 */

public interface SiteAnnotationService {
    String getSiteAnnotation() throws IOException;

    Result getSiteAnnotationFromFile(MultipartFile file, String pvalue, String str);

    Result getSnpMapAnalysisFile(MultipartFile file);

    Result getSnpMapAnalysisOut(String id, HttpServletResponse response);

    Result getEqtlSnpFile(MultipartFile uploadFile, String organization);
}
