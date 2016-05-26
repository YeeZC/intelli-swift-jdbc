package com.fr.bi.module;

import com.finebi.cube.api.ICubeDataLoaderCreator;
import com.finebi.cube.conf.BIDataSourceManagerProvider;
import com.finebi.cube.conf.BISystemPackageConfigurationProvider;
import com.fr.bi.etl.analysis.manager.BIAnalysisDataSourceManagerProvider;
import com.fr.stable.fun.Service;

/**
 * Created by 小灰灰 on 2015/12/11.
 */
public interface BIModule {
    void start();

    String getModuleName();

    //模块公用管理员的配置
    boolean isAllAdmin();

    BIDataSourceManagerProvider getDataSourceManagerProvider();
    //TODO Connery Analysis 这种大模块间的就别继承了。改动代价远大于写几行代码

    BIAnalysisDataSourceManagerProvider getAnalysisDataSourceManagerProvider();

    BISystemPackageConfigurationProvider getBusiPackManagerProvider();

    ICubeDataLoaderCreator getCubeDataLoaderCreator();

    public Service[] service4Register();
}