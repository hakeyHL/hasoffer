package hasoffer.core.test;

import hasoffer.base.utils.ExcelUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created on 2016/9/29.
 */
public class ExcelTest {

    @Test
    public void testExcelImport() throws IOException {

        File excel = new File("C:/Users/wing/Desktop/gift表格模板.xlsx");

        List<Map<String, String>> mapList = ExcelUtils.readRows(1, excel);

        System.out.println(mapList);

    }

}
