package hasoffer.core.persistence.po.admin;

import java.math.BigInteger;
import java.util.Date;

/**
 * Created on 2016/4/15.
 */
public class StsAliveResult {

        private String downloadDate;

        private BigInteger alives;

        private BigInteger alivesPercent;

        private BigInteger ratios;

        private BigInteger ratioPercent;

        public String getDownloadDate() {
            return downloadDate;
        }

        public void setDownloadDate(String downloadDate) {
            this.downloadDate = downloadDate;
        }

        public BigInteger getAlives() {
            return alives;
        }

        public void setAlives(BigInteger alives) {
            this.alives = alives;
        }

        public BigInteger getAlivesPercent() {
            return alivesPercent;
        }

        public void setAlivesPercent(BigInteger alivesPercent) {
            this.alivesPercent = alivesPercent;
        }

        public BigInteger getRatios() {
            return ratios;
        }

        public void setRatios(BigInteger ratios) {
            this.ratios = ratios;
        }

        public BigInteger getRatioPercent() {
            return ratioPercent;
        }

        public void setRatioPercent(BigInteger ratioPercent) {
            this.ratioPercent = ratioPercent;
        }

}
