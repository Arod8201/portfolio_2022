package com.portfolio.controller;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.entity.TStock;
import com.portfolio.repository.TStockRepository;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

@RestController
@RequestMapping("/price")
public class PriceController {

	@Autowired
	private TStockRepository tStockRepository;

	@GetMapping("/refresh")
	public List<TStock> refresh() {
		List<TStock> list = tStockRepository.findAll();
		for(TStock ts : list) {
			try {
				// 抓取報價資料
				Stock stock=YahooFinance.get(ts.getSymbol());
				ts.setChangeInPercent(stock.getQuote().getChangeInPercent());
				ts.setChangePrice(stock.getQuote().getChange());
				ts.setPreClosed(stock.getQuote().getPreviousClose());
				ts.setPrice(stock.getQuote().getPrice());
				ts.setVolumn(stock.getQuote().getVolume());
				ts.setTransactionDate(stock.getQuote().getLastTradeTime().getTime());
				// 更新報價資訊
				tStockRepository.updatePrice(
								 ts.getId(), 
								 ts.getChangePrice(), 
								 ts.getChangeInPercent(), 
								 ts.getPreClosed(), 
								 ts.getPrice(), 
								 ts.getTransactionDate(), 
								 ts.getVolumn());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	// 取得歷史資料繪製 k線圖用
	@GetMapping("/hisquotes/{symbol:.+}")
	public List<HistoricalQuote> queryHistoricalQuotes(@PathVariable("symbol") String symbol){
		List<HistoricalQuote> historicalQuotes = null;
		try {
			Calendar from = Calendar.getInstance();
			Calendar to = Calendar.getInstance();
			from.add(Calendar.YEAR, -1); // from 1 year ago 
			Stock stock = YahooFinance.get(symbol);
			historicalQuotes = stock.getHistory(from, to, Interval.DAILY); // DAILY -> 日線 // MONTHLY -> 月線
		} catch (Exception e) {
			e.printStackTrace();
		}
		return historicalQuotes;
	}

}
