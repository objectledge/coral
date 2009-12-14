scriptLoader.loadCommon('WinPopup.js');
scriptLoader.loadCommon('Forms.js');
scriptLoader.loadCommon('PropertySelector.js');

function selectEntity(attribute, form, element, baseLink)
{
  window.propertySelector = new PropertySelector(attribute, form, element);
  getWinPopup('Coral Browser').open(baseLink, 800, 400, 'center middle');
}
