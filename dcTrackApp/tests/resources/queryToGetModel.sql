SELECT model_id, model_name, sys_model_mfr_name, l.lkp_value_code, mounting,   form_factor, ru_height 
FROM dct_models m inner join dct_lks_data l on m.class_lks_id = l.lks_id
where model_id in (1,
100,
1015,
1020,
1255,
1304,
1434,
1974,
3593,
45,
46,
5126,
54,
58,
6400)
