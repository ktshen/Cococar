# -*- coding: utf-8 -*-
from __future__ import unicode_literals
from django import forms
from crispy_forms.helper import FormHelper
from crispy_forms.layout import Submit, Layout, Field, Button
from crispy_forms.bootstrap import PrependedText

DATE_TIME_FORMAT = "%Y/%m/%d %H:%M"


class SearchForm(forms.Form):
    location = forms.CharField(max_length=50)
    start_time = forms.DateTimeField(input_formats=DATE_TIME_FORMAT)
    end_time = forms.DateTimeField(input_formats=DATE_TIME_FORMAT)

    def __init__(self, *args, **kwargs):
        super(SearchForm, self).__init__(*args, **kwargs)
        self.helper = FormHelper(self)
        self.helper.form_id = 'record_form'
        self.helper.field_class = "input-group"
        self.helper.form_action = "/cococar/search"
        self.helper.layout = Layout(
            Field('location', placeholder="Search for location", ),
            Field('start_time'),
            Field('end_time'),
            Submit('submit', 'Submit', css_class='btn btn-info')
        )

