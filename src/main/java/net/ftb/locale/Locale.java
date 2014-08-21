package net.ftb.locale;

public enum Locale {
    cyGB, daDK, deDE,
    enUS, enGB, esES,
    fiFI, frFR, itIT,
    nlNL, noNO, maHU,
    ptBR, ptPT, ruRU,
    svSE, zhCN;

    public static Locale get(String key){
        try{
            return Locale.valueOf(key);
        } catch(Exception ex){
            return Locale.enUS;
        }
    }
}