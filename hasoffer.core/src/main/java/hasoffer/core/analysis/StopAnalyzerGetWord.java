package hasoffer.core.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Date : 2016/6/17
 * Function :
 */
public class StopAnalyzerGetWord implements IGetWord {

    @Override
    public String[] getWords(String text) {
        List<String> words = new ArrayList<String>();
        try {

            // 自定义停用词
            String[] self_stop_words = {""};
            CharArraySet cas = new CharArraySet(0, true);
            for (int i = 0; i < self_stop_words.length; i++) {
                cas.add(self_stop_words[i]);
            }

            // 加入系统默认停用词
            Iterator<Object> itor = StopAnalyzer.ENGLISH_STOP_WORDS_SET.iterator();
            while (itor.hasNext()) {
                cas.add(itor.next());
            }

            // 标准分词器(Lucene内置的标准分析器,会将语汇单元转成小写形式，并去除停用词及标点符号)
            StopAnalyzer sa = new StopAnalyzer(cas);

            TokenStream ts = sa.tokenStream("field", text);
            CharTermAttribute ch = ts.addAttribute(CharTermAttribute.class);

            ts.reset();
            while (ts.incrementToken()) {
                words.add(ch.toString());
            }
            ts.end();
            ts.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return words.toArray(new String[0]);
    }
}
