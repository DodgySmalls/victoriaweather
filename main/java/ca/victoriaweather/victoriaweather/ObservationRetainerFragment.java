package ca.victoriaweather.victoriaweather;

/*
 * When an activity serializes any significant amount of data in onSaveInstanceState() a huge amount of waste is passed through intents on activity transitions
 * Instead, we create an arbitrary fragment to retain the data so that it is not carelessly passed around
 */
public class ObservationRetainerFragment {
    //TODO implementation
}
