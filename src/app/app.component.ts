// Copyright (C) 2017 Julien Boz
// 
// This file is part of Focus IT - Timbrage.
// 
// Focus IT - Timbrage is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Focus IT - Timbrage is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with Focus IT - Timbrage.  If not, see <http://www.gnu.org/licenses/>.
//

import { Component } from '@angular/core';
import { Platform } from 'ionic-angular';
import { StatusBar } from '@ionic-native/status-bar';
import { SplashScreen } from '@ionic-native/splash-screen';
import { Globalization } from '@ionic-native/globalization';
import { InAppBrowser, InAppBrowserOptions } from '@ionic-native/in-app-browser';

import { TranslateService } from '@ngx-translate/core';
import * as moment from 'moment';

import { availableLanguages, defaultLanguage } from './i18n.constants';
import { CalendarPage } from "../pages/calendar/calendar";

@Component({
  templateUrl: 'app.html',
  providers: [CalendarPage]
})
export class MyApp {
  rootPage: any = 'TabsPage';

  constructor(platform: Platform, statusBar: StatusBar, splashScreen: SplashScreen, globalization: Globalization,
              public calendarCtrl: CalendarPage, public translate: TranslateService,
              private inAppBrowser: InAppBrowser) {
    this.setLang(defaultLanguage);

    platform.ready().then(() => {
      if ((<any>window).cordova) {
        globalization.getPreferredLanguage().then(result => {
          const language = this.getSuitableLanguage(result.value);
          this.setLang(language);
        });
      } else {
        let browserLanguage = translate.getBrowserLang() || defaultLanguage;
        const language = this.getSuitableLanguage(browserLanguage);
        this.setLang(language);
      }

      // Okay, so the platform is ready and our plugins are available.
      // Here you can do any higher level native things you might need.
      statusBar.styleDefault();
      splashScreen.hide();
    });
  }

  /**
   * Changement du type de vue.
   */
  changeMode(mode: string): void {
    this.calendarCtrl.changeMode(mode);
  }

  setLang(lang: string): void {
    this.translate.setDefaultLang(lang);
    moment.locale(lang);
  }

  getSuitableLanguage(language) {
    language = language.substring(0, 2).toLowerCase();
    return availableLanguages.some(x => x.code == language) ? language : defaultLanguage;
  }

  options: InAppBrowserOptions = {
    location: 'yes',//Or 'no' 
    hidden: 'no', //Or  'yes'
    clearcache: 'yes',
    clearsessioncache: 'yes',
    zoom: 'yes',//Android only ,shows browser zoom controls 
    hardwareback: 'yes',
    mediaPlaybackRequiresUserAction: 'no',
    shouldPauseOnSuspend: 'no', //Android only 
    closebuttoncaption: 'Close', //iOS only
    disallowoverscroll: 'no', //iOS only 
    toolbar: 'yes', //iOS only 
    enableViewportScale: 'no', //iOS only 
    allowInlineMediaPlayback: 'no',//iOS only 
    presentationstyle: 'pagesheet',//iOS only 
    fullscreen: 'yes',//Windows only    
  };

  openGithub(): void {
    this.inAppBrowser.create("https://github.com/jboz/timbrage", "_system", this.options);
  }
}
