/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/KontoInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/01/04 16:18:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Autosuggest-Feld zur Eingabe/Auswahl eines Kontos.
 */
public class KontoInput extends SelectInput
{
  private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @param konto ausgewaehltes Konto.
   * @throws RemoteException
   */
  public KontoInput(Konto konto) throws RemoteException
  {
    super(init(),konto);
    setPleaseChoose(i18n.tr("Bitte w�hlen..."));
    this.setComment("");
    KontoListener kl = new KontoListener();
    this.addListener(kl);
    
    // einmal ausloesen
    if (konto != null)
      kl.handleEvent(null);
  }
  
  /**
   * Initialisiert die Liste der Konten.
   * @return Liste der Konten.
   * @throws RemoteException
   */
  private static GenericIterator init() throws RemoteException
  {
    DBIterator it = Settings.getDBService().createList(Konto.class);
    it.setOrder("ORDER BY blz, kontonummer");
    return it;
  }

  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#format(java.lang.Object)
   */
  protected String format(Object bean)
  {
    if (bean == null)
      return null;
    
    if (!(bean instanceof Konto))
      return bean.toString();
    
    try
    {
      Konto k = (Konto) bean;
      StringBuffer sb = new StringBuffer();
      
      sb.append(i18n.tr("Kto. {0}",k.getKontonummer()));
      
      String blz = k.getBLZ();
      sb.append(" [");
      String bankName = HBCIUtils.getNameForBLZ(blz);
      if (bankName != null && bankName.length() > 0)
      {
        sb.append(bankName);
      }
      else
      {
        sb.append("BLZ ");
        sb.append(blz);
      }
      sb.append("] ");
      sb.append(k.getName());

      String bez = k.getBezeichnung();
      if (bez != null && bez.length() > 0)
      {
        sb.append(" - ");
        sb.append(bez);
      }
      
      if (k.getSaldoDatum() != null)
      {
        sb.append(", ");
        sb.append(i18n.tr("Saldo: {0} {1}", new String[]{HBCI.DECIMALFORMAT.format(k.getSaldo()),k.getWaehrung()}));
      }
      
      return sb.toString();
    }
    catch (RemoteException re)
    {
      Logger.error("unable to format address",re);
      return null;
    }
  }

  /**
   * Listener, der die Auswahl des Kontos ueberwacht und den Kommentar anpasst.
   */
  private class KontoListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {

      try {
        Object o = getValue();
        if (o == null || !(o instanceof Konto))
        {
          setComment("");
          return;
        }

        Konto konto = (Konto) o;
        String w = konto.getWaehrung();

        Date datum = konto.getSaldoDatum();
        if (datum != null)
          setComment(i18n.tr("Saldo: {0} {1} vom {2}", new String[]{HBCI.DECIMALFORMAT.format(konto.getSaldo()),w,HBCI.DATEFORMAT.format(datum)}));
        else
          setComment("");
      }
      catch (RemoteException er)
      {
        Logger.error("error while updating currency",er);
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei Ermittlung der W�hrung"));
      }
    }
  }

}


/**********************************************************************
 * $Log: KontoInput.java,v $
 * Revision 1.1  2009/01/04 16:18:22  willuhn
 * @N BUGZILLA 404 - Kontoauswahl via SelectBox
 *
 **********************************************************************/