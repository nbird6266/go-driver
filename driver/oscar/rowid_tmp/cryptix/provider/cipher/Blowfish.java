/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.cipher;

import cryptix.CryptixException;
import cryptix.provider.cipher.NativeLink;
import cryptix.provider.key.RawSecretKey;
import cryptix.util.core.ArrayUtil;
import cryptix.util.core.Debug;
import cryptix.util.core.Hex;
import cryptix.util.core.LinkStatus;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.Security;
import xjava.security.Cipher;
import xjava.security.InvalidParameterTypeException;
import xjava.security.SymmetricCipher;

public final class Blowfish
extends Cipher
implements SymmetricCipher {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel = Debug.getLevel("Blowfish");
    private static final PrintWriter err = Debug.getOutput();
    private static NativeLink linkStatus = new NativeLink("Blowfish", 2, 3);
    private long native_cookie;
    private Object native_lock;
    private static final int[] P0 = new int[]{608135816, -2052912941, 320440878, 57701188, -1542899678, 698298832, 137296536, -330404727, 1160258022, 953160567, -1101764913, 887688300, -1062458953, -914599715, 1065670069, -1253635817, -1843997223, -1988494565};
    private static final int[] S0 = new int[]{-785314906, -1730169428, 805139163, -803545161, -1193168915, 1780907670, -1166241723, -248741991, 614570311, -1282315017, 134345442, -2054226922, 1667834072, 1901547113, -1537671517, -191677058, 227898511, 1921955416, 1904987480, -2112533778, 2069144605, -1034266187, -1674521287, 720527379, -976113629, 677414384, -901678824, -1193592593, -1904616272, 1614419982, 1822297739, -1340175810, -686458943, -1120842969, 2024746970, 1432378464, -430627341, -1437226092, 1464375394, 1676153920, 1439316330, 715854006, -1261675468, 289532110, -1588296017, 2087905683, -1276242927, 1668267050, 732546397, 1947742710, -832815594, -1685613794, -1344882125, 1814351708, 2050118529, 680887927, 999245976, 1800124847, -994056165, 1713906067, 1641548236, -81679983, 1216130144, 1575780402, -276538019, -377129551, -601480446, -345695352, 596196993, -745100091, 258830323, -2081144263, 772490370, -1534844924, 1774776394, -1642095778, 566650946, -152474470, 1728879713, -1412200208, 1783734482, -665571480, -1777359064, -1420741725, 1861159788, 326777828, -1170476976, 2130389656, -1578015459, 967770486, 1724537150, -2109534584, -1930525159, 1164943284, 2105845187, 998989502, -529566248, -2050940813, 1075463327, 1455516326, 1322494562, 910128902, 469688178, 1117454909, 936433444, -804646328, -619713837, 1240580251, 122909385, -2137449605, 634681816, -152510729, -469872614, -1233564613, -1754472259, 79693498, -1045868618, 1084186820, 1583128258, 426386531, 1761308591, 1047286709, 322548459, 995290223, 1845252383, -1691314900, -863943356, -1352745719, -1092366332, -567063811, 1712269319, 422464435, -1060394921, 1170764815, -771006663, -1177289765, 1434042557, 442511882, -694091578, 1076654713, 1738483198, -81812532, -1901729288, -617471240, 1014306527, -43947243, 793779912, -1392160085, 842905082, -48003232, 1395751752, 1040244610, -1638115397, -898659168, 445077038, -552113701, -717051658, 679411651, -1402522938, -1940957837, 1767581616, -1144366904, -503340195, -1192226400, 284835224, -48135240, 1258075500, 768725851, -1705778055, -1225243291, -762426948, 1274779536, -505548070, -1530167757, 1660621633, -823867672, -283063590, 913787905, -797008130, 737222580, -1780753843, -1366257256, -357724559, 1804850592, -795946544, -1345903136, -1908647121, -1904896841, -1879645445, -233690268, -2004305902, -1878134756, 1336762016, 1754252060, -774901359, -1280786003, 791618072, -1106372745, -361419266, -1962795103, -442446833, -1250986776, 413987798, -829824359, -1264037920, -49028937, 2093235073, -760370983, 375366246, -2137688315, -1815317740, 555357303, -424861595, 2008414854, -950779147, -73583153, -338841844, 2067696032, -700376109, -1373733303, 2428461, 544322398, 577241275, 1471733935, 610547355, -267798242, 1432588573, 1507829418, 2025931657, -648391809, 545086370, 48609733, -2094660746, 1653985193, 298326376, 1316178497, -1287180854, 2064951626, 458293330, -1705826027, -703637697, -1130641692, 727753846, -2115603456, 146436021, 1461446943, -224990101, 705550613, -1235000031, -407242314, -13368018, -981117340, 1404054877, -1449160799, 146425753, 1854211946};
    private static final int[] S1 = new int[]{1266315497, -1246549692, -613086930, -1004984797, -1385257296, 1235738493, -1662099272, -1880247706, -324367247, 1771706367, 1449415276, -1028546847, 422970021, 1963543593, -1604775104, -468174274, 1062508698, 1531092325, 1804592342, -1711849514, -1580033017, -269995787, 1294809318, -265986623, 1289560198, -2072974554, 1669523910, 35572830, 157838143, 1052438473, 1016535060, 1802137761, 1753167236, 1386275462, -1214491899, -1437595849, 1040679964, 2145300060, -1904392980, 1461121720, -1338320329, -263189491, -266592508, 33600511, -1374882534, 1018524850, 629373528, -603381315, -779021319, 2091462646, -1808644237, 586499841, 988145025, 935516892, -927631820, -1695294041, -1455136442, 265290510, -322386114, -1535828415, -499593831, 1005194799, 847297441, 406762289, 1314163512, 1332590856, 1866599683, -167115585, 750260880, 613907577, 1450815602, -1129346641, -560302305, -644675568, -1282691566, -590397650, 1427272223, 778793252, 1343938022, -1618686585, 2052605720, 1946737175, -1130390852, -380928628, -327488454, -612033030, 1661551462, -1000029230, -283371449, 840292616, -582796489, 616741398, 312560963, 711312465, 1351876610, 322626781, 1910503582, 271666773, -2119403562, 1594956187, 70604529, -677132437, 1007753275, 1495573769, -225450259, -1745748998, -1631928532, 504708206, -2031925904, -353800271, -2045878774, 1514023603, 1998579484, 1312622330, 694541497, -1712906993, -2143385130, 1382467621, 776784248, -1676627094, -971698502, -1797068168, -1510196141, 503983604, -218673497, 907881277, 423175695, 432175456, 1378068232, -149744970, -340918674, -356311194, -474200683, -1501837181, -1317062703, 26017576, -1020076561, -1100195163, 1700274565, 1756076034, -288447217, -617638597, 720338349, 1533947780, 354530856, 688349552, -321042571, 1637815568, 332179504, -345916010, 53804574, -1442618417, -1250730864, 1282449977, -711025141, -877994476, -288586052, 1617046695, -1666491221, -1292663698, 1686838959, 431878346, -1608291911, 1700445008, 1080580658, 1009431731, 832498133, -1071531785, -1688990951, -2023776103, -1778935426, 1648197032, -130578278, -1746719369, 300782431, 375919233, 238389289, -941219882, -1763778655, 2019080857, 1475708069, 455242339, -1685863425, 448939670, -843904277, 1395535956, -1881585436, 1841049896, 1491858159, 885456874, -30872223, -293847949, 1565136089, -396052509, 1108368660, 540939232, 1173283510, -1549095958, -613658859, -87339056, -951913406, -278217803, 1699691293, 1103962373, -669091426, -2038084153, -464828566, 1031889488, -815619598, 1535977030, -58162272, -1043876189, 2132092099, 1774941330, 1199868427, 1452454533, 157007616, -1390851939, 342012276, 595725824, 1480756522, 206960106, 497939518, 591360097, 863170706, -1919713727, -698356495, 1814182875, 2094937945, -873565088, 1082520231, -831049106, -1509457788, 435703966, -386934699, 1641649973, -1452693590, -989067582, 1510255612, -2146710820, -1639679442, -1018874748, -36346107, 236887753, -613164077, 274041037, 1734335097, -479771840, -976997275, 1899903192, 1026095262, -244449504, 356393447, -1884275382, -421290197, -612127241};
    private static final int[] S2 = new int[]{-381855128, -1803468553, -162781668, -1805047500, 1091903735, 1979897079, -1124832466, -727580568, -737663887, 857797738, 1136121015, 1342202287, 507115054, -1759230650, 337727348, -1081374656, 1301675037, -1766485585, 1895095763, 1721773893, -1078195732, 62756741, 2142006736, 835421444, -1762973773, 1442658625, -635090970, -1412822374, 676362277, 1392781812, 170690266, -373920261, 1759253602, -683120384, 1745797284, 664899054, 1329594018, -393761396, -1249058810, 2062866102, -1429332356, -751345684, -830954599, 1080764994, 553557557, -638351943, -298199125, 991055499, 499776247, 1265440854, 648242737, -354183246, 980351604, -581221582, 1749149687, -898096901, -83167922, -654396521, 1161844396, -1169648345, 0x55533A3A, 545492359, -26498633, -795437749, 1437099964, -1592419752, -861329053, -1713251533, -1507177898, 1060185593, 1593081372, -1876348548, -34019326, 69676912, -2135222948, 86519011, -1782508216, -456757982, 1220612927, -955283748, 133810670, 1090789135, 1078426020, 1569222167, 845107691, -711212847, -222510705, 1091646820, 628848692, 1613405280, -537335645, 526609435, 236106946, 48312990, -1352249391, -892239595, 1797494240, 859738849, 992217954, -289490654, -2051890674, -424014439, -562951028, 765654824, -804095931, -1783130883, 1685915746, -405998096, 1414112111, -2021832454, -1013056217, -214004450, 172450625, -1724973196, 980381355, -185008841, -1475158944, -1578377736, -1726226100, -613520627, -964995824, 1835478071, 660984891, -590288892, -248967737, -872349789, -1254551662, 1762651403, 1719377915, -824476260, -1601057013, -652910941, -1156370552, 1364962596, 2073328063, 1983633131, 926494387, -871278215, -2144935273, -198299347, 1749200295, -966120645, 309677260, 2016342300, 1779581495, -1215147545, 111262694, 1274766160, 443224088, 298511866, 1025883608, -488520759, 1145181785, 168956806, -653464466, -710153686, 1689216846, -628709281, -1094719096, 1692713982, -1648590761, -252198778, 1618508792, 1610833997, -771914938, -164094032, 2001055236, -684262196, -2092799181, -266425487, -1333771897, 1006657119, 2006996926, -1108824540, 1430667929, -1084739999, 1314452623, -220332638, -193663176, -2021016126, 1399257539, -927756684, -1267338667, 1190975929, 2062231137, -1960976508, -2073424263, -1856006686, 1181637006, 548689776, -1932175983, -922558900, -1190417183, -1149106736, 296247880, 1970579870, -1216407114, -525738999, 1714227617, -1003338189, -396747006, 166772364, 1251581989, 493813264, 448347421, 195405023, -1584991729, 677966185, -591930749, 1463355134, -1578971493, 1338867538, 1343315457, -1492745222, -1610435132, 233230375, -1694987225, 2000651841, -1017099258, 1638401717, -266896856, -1057650976, 6314154, 819756386, 300326615, 590932579, 1405279636, -1027467724, -1144263082, -1866680610, -335774303, -833020554, 1862657033, 1266418056, 963775037, 2089974820, -2031914401, 1917689273, 448879540, -744572676, -313240200, 150775221, -667058989, 1303187396, 508620638, -1318983944, -1568336679, 1817252668, 1876281319, 1457606340, 908771278, -574175177, -677760460, -1838972398, 1729034894, 1080033504};
    private static final int[] S3 = new int[]{976866871, -738527793, -1413318857, 1522871579, 1555064734, 1336096578, -746444992, -1715692610, -720269667, -1089506539, -701686658, -956251013, -1215554709, 564236357, -1301368386, 1781952180, 1464380207, -1131123079, -962365742, 1699332808, 1393555694, 1183702653, -713881059, 1288719814, 691649499, -1447410096, -1399511320, -1101077756, -1577396752, 1781354906, 1676643554, -1702433246, -1064713544, 1126444790, -1524759638, -1661808476, -2084544070, -1679201715, -1880812208, -1167828010, 673620729, -1489356063, 1269405062, -279616791, -953159725, -145557542, 1057255273, 2012875353, -2132498155, -2018474495, -1693849939, 993977747, -376373926, -1640704105, 753973209, 36408145, -1764381638, 25011837, -774947114, 2088578344, 530523599, -1376601957, 1524020338, 1518925132, -534139791, -535190042, 1202760957, -309069157, -388774771, 674977740, -120232407, 2031300136, 2019492241, -311074731, -141160892, -472686964, 352677332, -1997247046, 60907813, 90501309, -1007968747, 1016092578, -1759044884, -1455814870, 457141659, 509813237, -174299397, 652014361, 1966332200, -1319764491, 55981186, -1967506245, 676427537, -1039476232, -1412673177, -861040033, 1307055953, 942726286, 933058658, -1826555503, -361066302, -79791154, 1361170020, 2001714738, -1464409218, -1020707514, 1222529897, 1679025792, -1565652976, -580013532, 1770335741, 151462246, -1281735158, 1682292957, 1483529935, 471910574, 1539241949, 458788160, -858652289, 1807016891, -576558466, 978976581, 1043663428, -1129001515, 1927990952, -94075717, -1922690386, -1086558393, -761535389, 1412390302, -1362987237, -162634896, 1947078029, -413461673, -126740879, -1353482915, 1077988104, 1320477388, 886195818, 18198404, -508558296, -1785185763, 112762804, -831610808, 1866414978, 891333506, 18488651, 661792760, 1628790961, -409780260, -1153795797, 876946877, -1601685023, 1372485963, 791857591, -1608533303, -534984578, -1127755274, -822013501, -1578587449, 445679433, -732971622, -790962485, -720709064, 54117162, -963561881, -1913048708, -525259953, -140617289, 1140177722, -220915201, 668550556, -1080614356, 367459370, 261225585, -1684794075, -85617823, -826893077, -1029151655, 314222801, -1228863650, -486184436, 282218597, -888953790, -521376242, 379116347, 1285071038, 846784868, -1625320142, -523005217, -744475605, -1989021154, 453669953, 1268987020, -977374944, -1015663912, -550133875, -1684459730, -435458233, 266596637, -447948204, 517658769, -832407089, -851542417, 370717030, -47440635, -2070949179, -151313767, -182193321, -1506642397, -1817692879, 1456262402, -1393524382, 1517677493, 1846949527, -1999473716, -560569710, -2118563376, 1280348187, 1908823572, -423180355, 846861322, 1172426758, -1007518822, -911584259, 1655181056, -1155153950, 901632758, 1897031941, -1308360158, -1228157060, -847864789, 1393639104, 373351379, 950779232, 625454576, -1170726756, -146354570, 2007998917, 544563296, -2050228658, -1964470824, 2058025392, 1291430526, 424198748, 50039436, 29584100, -689184263, -1865090967, -1503863136, 1057563949, -1039604065, -1219600078, -831004069, 1469046755, 985887462};
    private static final int DEFAULT_NOF_ROUNDS = 16;
    private static final int MIN_NOF_ROUNDS = 16;
    private static final int MAX_NOF_ROUNDS = 16;
    private static final int BLOCK_SIZE = 8;
    private static final int MIN_USER_KEY_LENGTH = 5;
    private static final int MAX_USER_KEY_LENGTH = 56;
    private int rounds;
    private int[] P;
    private int K0;
    private int K1;
    private int K2;
    private int K3;
    private int K4;
    private int K5;
    private int K6;
    private int K7;
    private int K8;
    private int K9;
    private int K10;
    private int K11;
    private int K12;
    private int K13;
    private int K14;
    private int K15;
    private int K16;
    private int K17;
    private int[] S;
    private static final boolean OK;
    private static final byte[][][] tests;

    static {
        int crc = 0;
        int i = 0;
        while (i < 18) {
            crc = (crc << 1 | crc >>> 31) + P0[i];
            ++i;
        }
        i = 0;
        while (i < 256) {
            crc *= 13;
            crc = (crc << 11 | crc >>> 21) + S0[i];
            ++i;
        }
        i = 0;
        while (i < 256) {
            crc *= 13;
            crc = (crc << 11 | crc >>> 21) + S1[i];
            ++i;
        }
        i = 0;
        while (i < 256) {
            crc *= 13;
            crc = (crc << 11 | crc >>> 21) + S2[i];
            ++i;
        }
        i = 0;
        while (i < 256) {
            crc *= 13;
            crc = (crc << 11 | crc >>> 21) + S3[i];
            ++i;
        }
        if (debuglevel >= 4) {
            Blowfish.debug("crc = " + Hex.intToString(crc));
        }
        boolean bl = OK = crc == 1434851937;
        if (debuglevel >= 3) {
            Blowfish.debug("Good CRC on initial P and S boxes? " + OK);
        }
        tests = new byte[][][]{new byte[][]{{97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122}, {66, 76, 79, 87, 70, 73, 83, 72}, {50, 78, -48, -2, -12, 19, -94, 3}}, new byte[][]{{87, 104, 111, 32, 105, 115, 32, 74, 111, 104, 110, 32, 71, 97, 108, 116, 63}, {-2, -36, -70, -104, 118, 84, 50, 16}, {-52, -111, 115, 43, -128, 34, -10, -124}}, new byte[][]{{65, 121, 110, -96, 82, 97, 110, -28}, {-2, -36, -70, -104, 118, 84, 50, 16}, {-31, 19, -12, 16, 44, -4, -50, 67}}};
    }

    private static void debug(String s) {
        err.println("Blowfish: " + s);
    }

    public static LinkStatus getLinkStatus() {
        return linkStatus;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void link() {
        NativeLink nativeLink = linkStatus;
        synchronized (nativeLink) {
            block8: {
                try {
                    if (linkStatus.attemptLoad()) {
                        linkStatus.checkVersion(Blowfish.getLibMajorVersion(), Blowfish.getLibMinorVersion());
                        linkStatus.check(this.native_clinit());
                    }
                    if (linkStatus.useNative()) {
                        linkStatus.check(this.native_init());
                        this.native_lock = new Object();
                    }
                }
                catch (UnsatisfiedLinkError e) {
                    linkStatus.fail(e);
                    if (debuglevel <= 2) break block8;
                    Blowfish.debug(e.getMessage());
                }
            }
            if (debuglevel > 2) {
                Blowfish.debug("Using native library? " + (this.native_lock != null));
            }
        }
    }

    private static native int getLibMajorVersion();

    private static native int getLibMinorVersion();

    private native String native_clinit();

    private native String native_init();

    private native String native_ks(long var1, byte[] var3, int var4);

    private native int native_crypt(long var1, byte[] var3, int var4, byte[] var5, int var6, boolean var7, int var8);

    private native String native_finalize();

    public Blowfish() {
        block4: {
            super(false, false, "Cryptix");
            this.rounds = 16;
            this.P = new int[18];
            this.S = new int[1024];
            if (!OK) {
                throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": CRC failed on initial data");
            }
            this.link();
            try {
                String ps = Security.getAlgorithmProperty("Blowfish", "rounds");
                if (ps != null) {
                    this.setRounds(Integer.parseInt(ps));
                }
            }
            catch (Exception e) {
                if (debuglevel <= 0) break block4;
                Blowfish.debug("Could not set number of rounds");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void finalize() {
        if (this.native_lock != null) {
            Object object = this.native_lock;
            synchronized (object) {
                String error = this.native_finalize();
                if (error != null) {
                    Blowfish.debug(String.valueOf(error) + " in native_finalize");
                }
            }
        }
    }

    public final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    protected int engineBlockSize() {
        return 8;
    }

    protected void engineInitEncrypt(Key key) throws InvalidKeyException, CryptixException {
        this.makeKey(key);
    }

    protected void engineInitDecrypt(Key key) throws InvalidKeyException, CryptixException {
        this.makeKey(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        Object newin;
        boolean doEncrypt;
        if (inLen < 0) {
            throw new IllegalArgumentException("inLen < 0");
        }
        int blockCount = inLen / 8;
        inLen = blockCount * 8;
        boolean bl = doEncrypt = this.getState() == 1;
        if (in == out && (outOffset >= inOffset && (long)outOffset < (long)inOffset + (long)inLen || inOffset >= outOffset && (long)inOffset < (long)outOffset + (long)inLen)) {
            newin = new byte[inLen];
            System.arraycopy(in, inOffset, newin, 0, inLen);
            in = newin;
            inOffset = 0;
        }
        if (this.native_lock != null) {
            newin = this.native_lock;
            synchronized (newin) {
                if (inOffset < 0 || (long)inOffset + (long)inLen > (long)((byte[])in).length || outOffset < 0 || (long)outOffset + (long)inLen > (long)out.length) {
                    throw new ArrayIndexOutOfBoundsException(String.valueOf(this.getAlgorithm()) + ": Arguments to native_crypt would cause a buffer overflow");
                }
                int i = 0;
                while (i < blockCount) {
                    if (this.native_crypt(this.native_cookie, (byte[])in, inOffset, out, outOffset, doEncrypt, this.rounds) == 0) {
                        throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Error in native code");
                    }
                    inOffset += 8;
                    outOffset += 8;
                    ++i;
                }
            }
        } else if (doEncrypt) {
            int i = 0;
            while (i < blockCount) {
                this.blockEncrypt((byte[])in, inOffset, out, outOffset);
                inOffset += 8;
                outOffset += 8;
                ++i;
            }
        } else {
            int i = 0;
            while (i < blockCount) {
                this.blockDecrypt((byte[])in, inOffset, out, outOffset);
                inOffset += 8;
                outOffset += 8;
                ++i;
            }
        }
        return inLen;
    }

    protected void engineSetParameter(String param, Object value) throws InvalidParameterException, InvalidParameterTypeException {
        if (!(value instanceof String)) {
            throw new InvalidParameterTypeException(String.valueOf(this.getAlgorithm()) + ": value is not a String");
        }
        try {
            if (param.equals("rounds")) {
                this.setRounds(Integer.parseInt((String)value));
            }
        }
        catch (Exception e) {
            throw new InvalidParameterException(e.toString());
        }
        throw new InvalidParameterException(String.valueOf(this.getAlgorithm()) + ": " + param);
    }

    protected Object engineGetParameter(String param) {
        if (param.equals("rounds")) {
            return Integer.toString(this.rounds);
        }
        return null;
    }

    public void setRounds(int r) {
        if (this.getState() != 0) {
            throw new IllegalStateException(String.valueOf(this.getAlgorithm()) + ": Cipher not in UNINITIALIZED state");
        }
        if (r < 16 || r > 16) {
            throw new IllegalArgumentException(String.valueOf(this.getAlgorithm()) + ": Invalid number of rounds");
        }
        this.rounds = r;
    }

    public int getRounds() {
        return this.rounds;
    }

    private void blockEncrypt(byte[] in, int off, byte[] out, int outOff) {
        int L = (in[off++] & 0xFF) << 24 | (in[off++] & 0xFF) << 16 | (in[off++] & 0xFF) << 8 | in[off++] & 0xFF;
        int R = (in[off++] & 0xFF) << 24 | (in[off++] & 0xFF) << 16 | (in[off++] & 0xFF) << 8 | in[off] & 0xFF;
        if (this.rounds != 16) {
            L ^= this.P[0];
            int i = 0;
            while (i < this.rounds) {
                R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.P[++i];
                L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.P[++i];
            }
            R ^= this.P[this.rounds + 1];
        } else {
            R ^= (this.S[(L ^= this.K0) >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K1;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K2;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K3;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K4;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K5;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K6;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K7;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K8;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K9;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K10;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K11;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K12;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K13;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K14;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K15;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K16;
            R ^= this.K17;
        }
        out[outOff++] = (byte)(R >>> 24);
        out[outOff++] = (byte)(R >>> 16);
        out[outOff++] = (byte)(R >>> 8);
        out[outOff++] = (byte)R;
        out[outOff++] = (byte)(L >>> 24);
        out[outOff++] = (byte)(L >>> 16);
        out[outOff++] = (byte)(L >>> 8);
        out[outOff] = (byte)L;
    }

    private void blockDecrypt(byte[] in, int off, byte[] out, int outOff) {
        int L = (in[off++] & 0xFF) << 24 | (in[off++] & 0xFF) << 16 | (in[off++] & 0xFF) << 8 | in[off++] & 0xFF;
        int R = (in[off++] & 0xFF) << 24 | (in[off++] & 0xFF) << 16 | (in[off++] & 0xFF) << 8 | in[off] & 0xFF;
        if (this.rounds != 16) {
            L ^= this.P[this.rounds + 1];
            int i = this.rounds;
            while (i > 0) {
                L ^= (this.S[(R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.P[i--]) >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.P[i--];
            }
            R ^= this.P[0];
        } else {
            R ^= (this.S[(L ^= this.K17) >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K16;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K15;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K14;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K13;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K12;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K11;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K10;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K9;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K8;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K7;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K6;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K5;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K4;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K3;
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.K2;
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.K1;
            R ^= this.K0;
        }
        out[outOff++] = (byte)(R >>> 24);
        out[outOff++] = (byte)(R >>> 16);
        out[outOff++] = (byte)(R >>> 8);
        out[outOff++] = (byte)R;
        out[outOff++] = (byte)(L >>> 24);
        out[outOff++] = (byte)(L >>> 16);
        out[outOff++] = (byte)(L >>> 8);
        out[outOff] = (byte)L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void makeKey(Key key) throws InvalidKeyException, CryptixException {
        byte[] userkey = key.getEncoded();
        if (userkey == null) {
            throw new InvalidKeyException(String.valueOf(this.getAlgorithm()) + ": Null user key");
        }
        int len = userkey.length;
        if (len < 5 || len > 56) {
            throw new InvalidKeyException(String.valueOf(this.getAlgorithm()) + ": Invalid user key length");
        }
        if (this.native_lock != null) {
            Object object = this.native_lock;
            synchronized (object) {
                try {
                    linkStatus.check(this.native_ks(this.native_cookie, userkey, this.rounds));
                    return;
                }
                catch (Error error) {
                    this.native_finalize();
                    this.native_lock = null;
                    if (debuglevel > 0) {
                        Blowfish.debug(error + ". Will use 100% Java.");
                    }
                }
            }
        }
        System.arraycopy(S0, 0, this.S, 0, 256);
        System.arraycopy(S1, 0, this.S, 256, 256);
        System.arraycopy(S2, 0, this.S, 512, 256);
        System.arraycopy(S3, 0, this.S, 768, 256);
        System.arraycopy(P0, 0, this.P, 0, this.rounds + 2);
        int[] block = new int[2];
        int st = 0;
        st = 0;
        while (st < 10) {
            this.BF_encrypt(block[0], block[1], block, 0);
            ++st;
        }
        if (block[0] != -1426174275 || block[1] != 651634172) {
            throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Self Test 1 failed: encrypt^10(0) = " + Hex.toString(block));
        }
        if (debuglevel >= 3) {
            Blowfish.debug("Self Test 1 Good");
        }
        while (--st >= 0) {
            this.BF_decrypt(block[0], block[1], block, 0);
        }
        if (block[0] != 0 || block[1] != 0) {
            throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Self Test 1 failed: decrypt^10(encrypt^10(0)) = " + Hex.toString(block));
        }
        int i = 0;
        int j = 0;
        while (i < this.rounds + 2) {
            int ri = 0;
            int k = 0;
            while (k < 4) {
                ri = ri << 8 | userkey[j++] & 0xFF;
                j %= len;
                ++k;
            }
            int n = i++;
            this.P[n] = this.P[n] ^ ri;
        }
        this.BF_encrypt(0, 0, this.P, 0);
        i = 2;
        while (i < this.rounds + 2) {
            this.BF_encrypt(this.P[i - 2], this.P[i - 1], this.P, i);
            i += 2;
        }
        this.BF_encrypt(this.P[this.rounds], this.P[this.rounds + 1], this.S, 0);
        i = 2;
        while (i < 1024) {
            this.BF_encrypt(this.S[i - 2], this.S[i - 1], this.S, i);
            i += 2;
        }
        st = 0;
        while (st < 10) {
            this.BF_encrypt(block[0], block[1], block, 0);
            ++st;
        }
        while (--st >= 0) {
            this.BF_decrypt(block[0], block[1], block, 0);
        }
        if (block[0] != 0 || block[1] != 0) {
            throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Self Test 2 failed: decrypt^10(encrypt^10(0)) = " + Hex.toString(block));
        }
        if (debuglevel >= 3) {
            Blowfish.debug("Self Test 2 Good");
        }
        if (this.rounds == 16) {
            this.K0 = this.P[0];
            this.K1 = this.P[1];
            this.K2 = this.P[2];
            this.K3 = this.P[3];
            this.K4 = this.P[4];
            this.K5 = this.P[5];
            this.K6 = this.P[6];
            this.K7 = this.P[7];
            this.K8 = this.P[8];
            this.K9 = this.P[9];
            this.K10 = this.P[10];
            this.K11 = this.P[11];
            this.K12 = this.P[12];
            this.K13 = this.P[13];
            this.K14 = this.P[14];
            this.K15 = this.P[15];
            this.K16 = this.P[16];
            this.K17 = this.P[17];
        }
    }

    private void BF_encrypt(int L, int R, int[] out, int outOff) {
        L ^= this.P[0];
        int i = 0;
        while (i < this.rounds) {
            R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.P[++i];
            L ^= (this.S[R >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.P[++i];
        }
        out[outOff++] = R ^ this.P[this.rounds + 1];
        out[outOff] = L;
    }

    private void BF_decrypt(int L, int R, int[] out, int outOff) {
        L ^= this.P[this.rounds + 1];
        int i = this.rounds;
        while (i > 0) {
            L ^= (this.S[(R ^= (this.S[L >>> 24 & 0xFF] + this.S[256 + (L >>> 16 & 0xFF)] ^ this.S[512 + (L >>> 8 & 0xFF)]) + this.S[768 + (L & 0xFF)] ^ this.P[i--]) >>> 24 & 0xFF] + this.S[256 + (R >>> 16 & 0xFF)] ^ this.S[512 + (R >>> 8 & 0xFF)]) + this.S[768 + (R & 0xFF)] ^ this.P[i--];
        }
        out[outOff++] = R ^ this.P[0];
        out[outOff] = L;
    }

    public static void main(String[] args) {
        try {
            Blowfish.self_test();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void self_test() throws Exception {
        Cipher cryptor = Cipher.getInstance("Blowfish", "Cryptix");
        int i = 0;
        while (i < tests.length) {
            RawSecretKey userKey = new RawSecretKey("Blowfish", tests[i][0]);
            cryptor.initEncrypt(userKey);
            byte[] tmp = cryptor.crypt(tests[i][1]);
            if (!ArrayUtil.areEqual(tests[i][2], tmp)) {
                throw new CryptixException("encrypt #" + i + " failed");
            }
            cryptor.initDecrypt(userKey);
            tmp = cryptor.crypt(tests[i][2]);
            if (!ArrayUtil.areEqual(tests[i][1], tmp)) {
                throw new CryptixException("decrypt #" + i + " failed");
            }
            ++i;
        }
        if (debuglevel > 0) {
            Blowfish.debug("Self-test OK");
        }
    }
}

