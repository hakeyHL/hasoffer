package hasoffer.core.test;

import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.match.HasTag;
import jodd.io.FileUtil;
import org.junit.Test;

import java.io.File;

/**
 * Date : 2016/6/15
 * Function :
 */
public class TagTest {

    @Test
    public void importTags() throws Exception {
        String filePath = "d:/TMP/tags/catetag.txt";

        String[] lines = FileUtil.readLines(new File(filePath));

        for (String line : lines) {
            HasTag tag = cleanBySplit(line);

            System.out.println(tag);
        }
    }

    private HasTag cleanBySplit(String line) {

        String[] tags = line.split(",");

        String keyword = tags[0].trim();

        HasTag ht = new HasTag();
        ht.setTag(keyword);

        int len = tags.length;
        if (len > 1) {
            StringBuffer sb = new StringBuffer();
            for (int i = 1; i < len; i++) {
                String tag = tags[i].trim();
                if (!StringUtils.isEmpty(tag)) {
                    sb.append(tag).append(",");
                }
            }

            if (sb.length() > 0) {
                if (sb.charAt(sb.length() - 1) == ',') {
                    sb.delete(sb.lastIndexOf(","), sb.length());
                }

                ht.setAlias(sb.toString());
            }
        }

        return ht;
    }

}
