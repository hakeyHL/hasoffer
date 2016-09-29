package hasoffer.admin.controller;

import hasoffer.base.utils.ExcelUtils;
import hasoffer.core.admin.IGiftService;
import hasoffer.core.persistence.po.app.HasofferCoinsExchangeGift;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Created on 2016/9/29.
 */
@Controller
@RequestMapping(value = "/gift")
public class GiftController {

    @Resource
    IGiftService giftService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {

        ModelAndView modelAndView = new ModelAndView("/gift/list");

        return modelAndView;
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String fileName = "gift表格模板.xlsx";
            String downloadPath = request.getSession().getServletContext().getRealPath("/") + "/download/" + fileName;//获取下载模版路径
            File file = new File(downloadPath);
            toDownload(request, response, new FileInputStream(downloadPath), file, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 提供文件下载
     *
     * @param inputStream
     * @param file
     * @param fileName
     * @return
     */
    public void toDownload(HttpServletRequest request, HttpServletResponse response, FileInputStream inputStream,
                           File file, String fileName) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        fileName = new String(fileName.getBytes("GBK"), "ISO8859-1");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/x-msdownload");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.setHeader("Content-Length", String.valueOf(file.length()));
        try {
            in = new BufferedInputStream(inputStream);
            out = new BufferedOutputStream(response.getOutputStream());
            byte[] data = new byte[2048];
            int len = 0;
            while (-1 != (len = in.read(data, 0, data.length))) {
                out.write(data, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public ModelAndView importExcel(MultipartFile excel) {

        ModelAndView modelAndView = new ModelAndView();

        File file = (File) excel;

        try {

            List<Map<String, String>> mapList = ExcelUtils.readRows(1, file);

            for (Map<String, String> mapinfo : mapList) {

                HasofferCoinsExchangeGift gift = new HasofferCoinsExchangeGift();

                gift.setTitle(mapinfo.get("0"));
                gift.setRePrice(Float.parseFloat(mapinfo.get("1")));
                gift.setRePrice(Float.parseFloat(mapinfo.get("2")));

                giftService.createGift(gift);

            }

        } catch (IOException e) {

        }

        return modelAndView;
    }

}
