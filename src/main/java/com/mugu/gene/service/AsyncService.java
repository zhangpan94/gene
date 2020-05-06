package com.mugu.gene.service;


/**
 * @Author : zp
 * @Description :
 * @Date : 2019/11/4
 */
public interface AsyncService {

    void generateResult(String file, String fileout);

    void generateSnpResult(String file, String fileout, String organization);
}
