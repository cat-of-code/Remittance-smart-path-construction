package com.zh2.training.controller;

import com.zh2.training.domain.agentbank.BankRepository;
import com.zh2.training.domain.agentbank.MsgPathCalculateService;
import com.zh2.training.domain.message.Message;
import com.zh2.training.domain.message.MessageHandleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Bugott
 */
@RestController
public class CalculateController {

    @Autowired
    MsgPathCalculateService msgPathCalculateService;

    @Autowired
    MessageHandleService messageHandleService;

    @Autowired
    BankRepository bankRepository;

    @PostMapping(path = "/calculate")
    public ModelMap calculate(@RequestBody String messageStr) {
        //初始化结果列表
        ModelMap modelMap = new ModelMap();
        //调用服务拆分报文
//        Message message = messageHandleService.analyse(messageStr);
        Message message = messageHandleService.analyse("{1:F01ICBKCNBJ13700001161255}{2:O1031924200721KODBKRSE237000012007212007211924N}{4:\n" +
                ":20:CWHZ202007211924\n" +
                ":23B:CRED\n" +
                ":50K:/1234\n" +
                "SMITH,BEIJING\n" +
                ":52A:BNPAFRPA110\n" +
                ":56A:INGBHOAM100\n" +
                ":57A:CHASBRBR160\n" +
                ":59:/2224\n" +
                "POLLY,HONGKONG\n" +
                ":71A:OUR\n" +
                "}");
        //调用服务获取排序后的汇款清算路径
        ArrayList<ArrayList<ArrayList<String>>> paths = msgPathCalculateService.calculate(message);
        //循环获取前3条路径
        for (int i = 0; i < 3; i++) {
            //拿到第i条路径
            ArrayList<ArrayList<String>> path = paths.get(i);
            //final为给前端的单条路径格式
            HashMap<String, List<String>> finalPath = new HashMap<>(16);
            //TODO 在此拼接付款人信息

            //拼接来报行信息到路径信息中
            ArrayList<String> sourceBank = new ArrayList<>(2);
            sourceBank.add(bankRepository.getBankByBic(message.getSourceBankBic()).getCity());
            sourceBank.add(message.getSourceBankBic());
            finalPath.put("sourceBank",sourceBank);
            //拼接我行信息到路径信息中
            ArrayList<String> ourBank = new ArrayList<>(2);
            ourBank.add(bankRepository.getBankByBic(message.getOurBankBic()).getCity());
            ourBank.add(message.getOurBankBic());
            finalPath.put("ourBank",ourBank);
            for (int j = 0; j < path.size()-1; j++) {
                finalPath.put("agentBank_"+(j+1),path.get(j));
            }
            //如有中间行则拼接中间行行信息到路径信息中
            if(message.getMiddleBank() != null || !("".equals(message.getMiddleBank()))){
                ArrayList<String> middleBank = new ArrayList<>(2);
                middleBank.add(bankRepository.getBankByBic(message.getMiddleBank()).getCity());
                middleBank.add(message.getMiddleBank());
                finalPath.put("middleBank",middleBank);
            }
            //拼接收款行信息到路径信息中
            ArrayList<String> aimBank = new ArrayList<>(2);
            aimBank.add(bankRepository.getBankByBic(message.getAimBank()).getCity());
            aimBank.add(message.getAimBank());
            finalPath.put("aimBank",aimBank);
            //TODO 在此拼接收款人信息

            modelMap.addAttribute(i+"",finalPath);
        }
        return modelMap;
    }
}


//{
//        "cost":1,
//        "Debitor":["Amsterdam",["Amy","AmyAddress","AmyAccount"]],
//        "sourceBank":["Athens","AAAA"],
//        "ourBank":["Beijing","ICBCBJ"],
//        "agentBank_1":["Singapore","SSSSS"],
//        "agentBank_2":["Sydney","SYSS"],
//        "middleBank":["Vilnius","Vvvv"],
//        "aimBank":["Munich","MMMMMM"],
//        "Creditor":["Riga",["Reo","ReoAddress","ReoAccount"]]
//        },
//        {
//        "cost":2,
//        "Debitor":["Amsterdam",["Amy","AmyAddress","AmyAccount"]],
//        "sourceBank":["Athens","AAAA"],
//        "ourBank":["Beijing","ICBCBJ"],
//        "agentBank_1":["Tokyo","TTTT"],
//        "agentBank_2":["Berlin","BBBB"],
//        "agentBank_3":["Vienna","VVVVV"],
//        "aimBank":["Munich","MMMMMM"],
//        "Creditor":["Riga",["Reo","ReoAddress","ReoAccount"]]
//        },
//        {
//        "cost":3,
//        "Debitor":["Amsterdam",["Amy","AmyAddress","AmyAccount"]],
//        "sourceBank":["Athens","AAAA"],
//        "ourBank":["Beijing","ICBCBJ"],
//        "aimBankCustomer":["Munich","MMMMMM"],
//        "Creditor":["Riga",["Reo","ReoAddress","ReoAccount"]]
//        }
//        ]