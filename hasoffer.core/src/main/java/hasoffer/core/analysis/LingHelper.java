package hasoffer.core.analysis;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import hasoffer.core.bo.match.TagType;

/**
 * Created by chevy on 2016/7/3.
 */
public class LingHelper {

    private static MapDictionary<String> dictionary = new MapDictionary<String>();

    private static ExactDictionaryChunker dictionaryChunkerTT = new ExactDictionaryChunker(
            dictionary,
            IndoEuropeanTokenizerFactory.INSTANCE,
            false,
            false
    );

    public static void addToDict(String tag, TagType tagType, double score) {
        dictionary.addEntry(new DictionaryEntry<String>(tag, tagType.name(), score));
    }

    public static void analysis(String text) {
        Chunking chunking = dictionaryChunkerTT.chunk(text);
        for (Chunk chunk : chunking.chunkSet()) {
            int start = chunk.start();
            int end = chunk.end();
            String type = chunk.type();
            double score = chunk.score();
            String phrase = text.substring(start, end);
            System.out.println(text +
                    "  phrase=|" + phrase + "|"
                    + " start=" + start
                    + " end=" + end
                    + " type=" + type
                    + " score=" + score);
        }
    }

    public static void clearDict() {
        dictionary.clear();
    }

    public static int getDictSize() {
        return dictionary.size();
    }
}
