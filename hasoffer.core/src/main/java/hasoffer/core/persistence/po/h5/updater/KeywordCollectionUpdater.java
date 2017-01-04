package hasoffer.core.persistence.po.h5.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.h5.KeywordCollection;

public class KeywordCollectionUpdater extends Updater<String, KeywordCollection> {
    public KeywordCollectionUpdater(String id) {
        super(KeywordCollection.class, id);
    }
}
