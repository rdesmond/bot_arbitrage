package co.codingnomads.bot.arbitrage.service;

import co.codingnomads.bot.arbitrage.model.*;
import co.codingnomads.bot.arbitrage.model.exchange.ExchangeSpecs;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * Created by Thomas Leruth on 12/14/17
 */

/**
 * the arbitrage bot class
 */
@Service
public class Arbitrage {

//    @Autowired
//    DataUtil dataUtil;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // todo Email action (Kevin later)
    // todo trade action (Thomas)
    // todo make this method running every X minutes (Kevin)
    // todo fix the issue with autowired (ryan)
    // todo look more into the fee
    // Question: could thread the bid and ask finder but I guess gain would be minimum as list is very small

    // Margin at which we take the risk of running the arbitrage it covers
    // 1) Fee (which I can't seem to be able to pull from API, somebody?) (check Kevin and hack the code)
    // 2) Delay leading to movement in bid/ask spread
    /**
     * A trading arbitrage bot with multiple arbitrage action
     * @param currencyPair the pair selected
     * @param selectedExchanges an ArrayList of ExchangeEnum has to be added
     * @param arbitrageMargin the margin in which arbitrage is accepted (cove the trading fee and delay leading to mov in bid/ask
     * @param arbitrageActionSelection Pojo including 3 booleans for the potential outcomes
     * @throws IOException
     */
    public void run(CurrencyPair currencyPair,
                    ArrayList<ExchangeSpecs> selectedExchanges,
                    double arbitrageMargin,
                    ArbitrageActionSelection arbitrageActionSelection,
                    double baseNeed,
                    double counterNeed) throws IOException, InterruptedException {

        ExchangeGetter exchangeGetter = new ExchangeGetter();

        ArrayList<ActivatedExchange> activatedExchanges = exchangeGetter.getAllSelectedExchangeServices(selectedExchanges);

        // todo autowire it
        ExchangeDataGetter exchangeDataGetter = new ExchangeDataGetter();

        // temporary
        int i = 0;
        int loop = 1;

        while (i < loop) {
            ArrayList<BidAsk> listBidAsk = exchangeDataGetter.getAllBidAsk(activatedExchanges, currencyPair, baseNeed, counterNeed);

            // todo handle with a custom exception on the getAllBidAsk
            if (listBidAsk.size() == 0) {
                logger.warn("This pair is not traded on the exchanged selected");
                // System.out.println("This pair is not traded on the exchange selected");
                return;
            }

            // temporary
            System.out.println();
            System.out.println("Pulled Data");
            for (BidAsk bidAsk : listBidAsk) {
                System.out.println(bidAsk.toString());
            }
            System.out.println();

            // todo autowire it
            DataUtil dataUtil = new DataUtil();

            BidAsk lowAsk = dataUtil.lowAskFinder(listBidAsk);
            BidAsk highBid = dataUtil.highBidFinder(listBidAsk);

            // temporary
            System.out.println("Sorted result");
            System.out.println("the lowest ask is on " + lowAsk.getExchangeName() + " at " + lowAsk.getAsk());
            System.out.println("the highest bid is on " + highBid.getExchangeName() + " at " + highBid.getBid());
            System.out.println();

            BigDecimal difference = highBid.getBid().divide(lowAsk.getAsk(), 3, RoundingMode.HALF_DOWN);

            // todo autowire it
            ArbitrageAction arbitrageAction = new ArbitrageAction();

            if (arbitrageActionSelection.isPrintActionFlag()) {
                arbitrageAction.print(lowAsk, highBid, difference, arbitrageMargin);
            }
            if (arbitrageActionSelection.isEmailActionFlag()) {
                arbitrageAction.email();
            }
            if (arbitrageActionSelection.isTradeActionFlag()) {
                arbitrageAction.trade();
            }
            i++;
            Thread.sleep(5000);
        }
    }
}