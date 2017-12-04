// TODO: move the QR stuff to his own module
let numberOfQrsGenerated = 0;
const qrCardTemplate = qrNumber => `
    <div class="row">
      <div class="col-md-4 offset-md-4">
        <div class="card">
          <div class="row">
            <div class="col-md-6">
              <img id="qr-img-${qrNumber}" src="images/default-placeholder.png" class="img-fluid"
                   alt="Generated QR">
            </div>
            <div class="col-md-5 align-self-center">
              <form id="qr-generator-${qrNumber}">
                <div class="form-group row">
                  <label for="qr-foreground-color-${qrNumber}" class="col-md-6 col-form-label">Foreground</label>
                  <div class="col-md-6">
                    <input type="color" class="form-control" id="qr-foreground-color-${qrNumber}" value="#000000">
                  </div>
                </div>
                <div class="form-group row">
                  <label for="qr-background-color-${qrNumber}" class="col-md-6 col-form-label">Background</label>
                  <div class="col-md-6">
                    <input type="color" class="form-control" id="qr-background-color-${qrNumber}" value="#ffffff">
                  </div>
                </div>
                <div class="form-group row">
                  <label for="qr-logo-${qrNumber}" class="col-md-6 col-form-label">Logo</label>
                  <div class="col-md-6">
                    <input type="file" class="form-control" id="qr-logo-${qrNumber}">
                    <input type="hidden" id="qr-logo-file-${qrNumber}" value="">
                  </div>
                </div>
                <div class="row">
                  <div class="col-md-6 offset-md-3">
                    <button type="submit" class="btn btn-primary">Generate</button>
                  </div>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
    <br>`;

function qrGeneratorHandler(qrNumber) {
  return (ev) => {
    ev.preventDefault();
    const data = $("input[name='url']").val();
    if (data) {
      const logoFileValue = $(`#qr-logo-file-${qrNumber}`).val();
      const logoId = logoFileValue || null;
      const fgColor = $(`#qr-foreground-color-${qrNumber}`).val();
      const config = {
        bodyColor: fgColor,
        bgColor: $(`#qr-background-color-${qrNumber}`).val(),
        eye1Color: fgColor,
        eye2Color: fgColor,
        eye3Color: fgColor,
        eyeBall1Color: fgColor,
        eyeBall2Color: fgColor,
        eyeBall3Color: fgColor,
        logo: logoId,
      };
      const configStr = JSON.stringify(config);
      const url = `https://qr-generator.qrcode.studio/qr/custom?data=${encodeURIComponent(data)}&config=${encodeURIComponent(configStr)}`;
      $(`#qr-img-${qrNumber}`).attr('src', url);
    }
  };
}

function logoChangeHandler(qrNumber) {
  return (ev) => {
    const formData = new FormData();
    formData.append('file', ev.target.files[0]);
    $.ajax({
      url: 'https://qr-generator.qrcode.studio/qr/uploadimage',
      method: 'POST',
      contentType: false,
      data: formData,
      dataType: 'json',
      processData: false,
      success(msg) {
        $(`#qr-logo-file-${qrNumber}`).val(msg.file);
      },
    });
  };
}

$(document).ready(() => {
  $('#shortener').submit((event) => {
    event.preventDefault();
    $.ajax({
      url: '/link',
      type: 'POST',
      data: $(event.currentTarget).serialize(),
      success(msg) {
        $('#result').html(`<div class='alert alert-success lead'><a target='_blank' href='${msg.uri}'>${msg.uri}</a></div>`);
      },
      error() {
        $('#result').html("<div class='alert alert-danger lead'>ERROR</div>");
      },
    });
  });
  $('#generate-new-qr').click(() => {
    $('#qr-adder').before(qrCardTemplate(numberOfQrsGenerated));
    $(`#qr-generator-${numberOfQrsGenerated}`).submit(qrGeneratorHandler(numberOfQrsGenerated));
    $(`#qr-logo-${numberOfQrsGenerated}`).change(logoChangeHandler(numberOfQrsGenerated));
    numberOfQrsGenerated += 1;
    if (numberOfQrsGenerated > 2) {
      $('#qr-adder').remove();
    }
  });
});
