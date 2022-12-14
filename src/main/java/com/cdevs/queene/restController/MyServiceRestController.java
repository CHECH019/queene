package com.cdevs.queene.restController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdevs.queene.generics.GenericRestController;
import com.cdevs.queene.generics.GenericServiceApi;
import com.cdevs.queene.global.Constants;
import com.cdevs.queene.model.MyService;
import com.cdevs.queene.service.api.MyServiceServiceAPI;

@RestController
@RequestMapping(value = Constants.BASE_URL + "service")
public class MyServiceRestController extends GenericRestController<MyService,Integer>{
    
    @Autowired
    private MyServiceServiceAPI service;

    @Override
    public GenericServiceApi<MyService, Integer> getService() {
        return service;
    }
   
}