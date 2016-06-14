package hasoffer.core.test;

import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.StringUtils;
import org.junit.Test;

/**
 * Created on 2016/5/16.
 */
public class StringTest {

    @Test
    public void testStr1() {

        String str = "/gp/slredirect/redirect.html/ref=pa_sp_atf_aps_sr_pg1_1?pl=FRWcQVS5IPm8DzL%2B9KtwLxWqh7%2BsPygdWxJ3D1q1FpSj0whtJhEJuCM%2FAsFeUYEtDq7RphcrklI%2F%0AoUZE2vGaHoXIJcoi90F30Vh6k6WUHxzxm5tYtQjb%2BbxV%2FeWTHlmIjKMc%2B%2FgIcGu31fcBXg3GDsOm%0AoCKuZL1jC1IAb2p76Jw5VzKu0L41%2FHn%2BPzcKSfD9nPq%2F6ezTAS6TkYTXOHKQL4CJ6buFWpa4bQsE%0ATtCEQYM2nZydqskrNCcUItUJcfm8EymXbVoIq68y0GfK25BJ8L475G1%2FMLUIRLnIlJm15rmg0VTA%0Ao4mPVlJ%2F1YoY0zn1stk6D88rMN4BA7fT3CwKKSVl2K2w5ioN0Vg%2BLrz9e9Dy3o456mY3P8Esrbpy%0A5Avo39ElSua9lQr%2B7mk0R8bPmKC8AesnUIpS74FD7IT9w6Dml89pJUgt2JXFPoNvKO0pxvZQzVMm%0A8%2FU%2BvOZiUHUECznSC3s6nt5Kqy83b8iP3UEamI%2B1oj9iQWT%2FTOTy0Fad3qR5%2BxsfthMMytwa4jj6%0AJ5wytoINDZBRotiHw4wCu2kYb%2BwPk8BfMOXBpiWXjUiP4KxuYU%2BxzyK8%2BkvU5hhkNtNRXQnEJ1Zy%0AJGwDc%2FZITkiD%2BnQnjAaRj8im0kfjcvXzAtG5Ea5A1x1Jy2SrrlXqX4sXWOV7P8z6JMTAPM7ofYKQ%0A8JxpFcoWhIQaue4opwt%2FOFo%2F4PQVy0Ba4fwBwYE%2FSupJV4%2BIuzIuS8Jb75V%2Bci5RmOOwuM2qdrFO%0AgHgBFTrUIDFIruYsm51ZJiR4uURfI4pCataw%2B1A1qvsu%2FrqIjJnRJSczyYH7qZdv%2BpqLU6m7zZkB%0A7vXNlDS9PnsYo0jk%2FSHzCZ41Mh89RmlpxPNwY4xis512Eanptw%2BNCXn9EIFPOHvZLYwbf6K5Fzw9%0AFhbMuwvS7F19GuWgt%2Fk54QTnF2tB%2BA3%2FWze4AX13cRw2fI7dQZIJvLzVDYjNaq7JE67XTNArGuTC%0Aj0EXtNhnLNpcvIyf49ag%2FpJWHFE0lfJLxDlPIor7%2FW6bChWCDjHLd631fpAncA32dCpQWE4Hq5W9%0Azt%2FGCI8nVyx8aWjzOTBPsRdYH1n9t4Q8zBkF1TOi%2BbS%2BF4b6k0yWiUw6YAQoRFfl%2FwJVqng3i1bA%0ABJI7jaSGKWF3Zrvv7tw1ODqdJUf5HilKFggp%2Bln9ET%2FQuHWW1NoSglYLauU2%2BprB6A%3D%3D&token=0EA3486BC3CE4C876BBBFCECAD1E15943940BE9F";

        str = StringUtils.urlDecode(str);

        System.out.println(str);

    }

    @Test
    public void testStr2() {

        String str = "PrixCracker 65 Tempered Glass for Xiaomi Redmi 2";
        String str0 = "Chef'n Stainless Steel Apple, Strawberry, Watermelon Slicer";

        String str1 = HexDigestUtil.md5(StringUtils.getCleanChars(str0));

        System.out.println(str1);

    }


    @Test
    public void testStr3() {
//Adcom Thunder A-500(White, 512 MB)                                                  | 2bf8f236340ae154f8c5f73ee9e959d0
        String str = " Aruba Women's G-string Panty(Pack of 2)";
        Website website = Website.FLIPKART;

        System.out.println(HexDigestUtil.md5(website.name() + StringUtils.getCleanChars(str)));
    }

    @Test
    public void testStr4(){

        String str = "545";

        System.out.println(str.toLowerCase());

    }

}
